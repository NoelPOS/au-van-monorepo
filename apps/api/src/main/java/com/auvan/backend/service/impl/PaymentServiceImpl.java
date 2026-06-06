package com.auvan.backend.service.impl;

import com.auvan.backend.mapper.EntityMappers;

import com.auvan.backend.dto.request.ReviewPaymentRequest;
import com.auvan.backend.dto.request.SubmitPaymentProofRequest;
import com.auvan.backend.dto.response.PageResponse;
import com.auvan.backend.dto.response.PaymentResponse;
import com.auvan.backend.entity.Booking;
import com.auvan.backend.entity.Payment;
import com.auvan.backend.entity.Seat;
import com.auvan.backend.enums.BookingStatus;
import com.auvan.backend.enums.PaymentStatus;
import com.auvan.backend.event.BookingConfirmedEvent;
import com.auvan.backend.event.PaymentCompletedEvent;
import com.auvan.backend.event.PaymentFailedEvent;
import com.auvan.backend.exception.ConflictException;
import com.auvan.backend.exception.ForbiddenException;
import com.auvan.backend.exception.ResourceNotFoundException;

import com.auvan.backend.repository.BookingRepository;
import com.auvan.backend.repository.PaymentRepository;
import com.auvan.backend.service.AuditLogService;
import com.auvan.backend.service.PaymentService;
import com.auvan.backend.service.ReminderService;
import com.auvan.backend.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final SeatService seatService;
    private final ReminderService reminderService;
    private final AuditLogService auditLogService;
    private final EntityMappers mappers;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(UUID userId) {
        return mappers.toPaymentList(
                paymentRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getById(UUID paymentId) {
        return mappers.toPayment(findOrThrow(paymentId));
    }

    @Override
    @Transactional
    public PaymentResponse submitProof(UUID bookingId, UUID userId, SubmitPaymentProofRequest request) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment for booking", bookingId));

        if (!payment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this payment");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ConflictException("Payment proof can only be submitted for pending payments");
        }

        payment.setProofImageUrl(request.proofImageUrl());
        payment.setProofReference(request.proofReference());
        payment.setProofSubmittedAt(Instant.now());
        if (request.paidAt() != null) {
            payment.setPaidAt(request.paidAt());
        }
        payment.setStatus(PaymentStatus.PENDING_REVIEW);

        payment.getBooking().setStatus(BookingStatus.PAYMENT_UNDER_REVIEW);
        bookingRepository.save(payment.getBooking());

        return mappers.toPayment(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponse reviewPayment(
            UUID paymentId,
            UUID adminId,
            ReviewPaymentRequest request,
            String ip,
            String userAgent
    ) {
        Payment payment = findOrThrow(paymentId);
        Booking booking = payment.getBooking();

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new ConflictException("Payment has already been approved");
        }

        PaymentStatus newStatus = request.status();
        Set<PaymentStatus> allowedStatuses = Set.of(PaymentStatus.COMPLETED, PaymentStatus.FAILED, PaymentStatus.REFUNDED);
        if (!allowedStatuses.contains(newStatus)) {
            throw new ConflictException("Review status must be COMPLETED, FAILED, or REFUNDED");
        }

        payment.setStatus(newStatus);
        payment.setReviewedBy(adminId);
        payment.setReviewedAt(Instant.now());
        payment.setReviewNote(request.reviewNote());

        if (request.transactionId() != null) {
            payment.setTransactionId(request.transactionId());
        }

        if (newStatus == PaymentStatus.COMPLETED) {
            approvePayment(payment, booking);
        } else {
            rejectPayment(payment, booking);
        }

        payment = paymentRepository.save(payment);

        auditLogService.log(
                adminId,
                "payment_reviewed",
                "Payment",
                paymentId.toString(),
                Map.of(
                        "status", newStatus.name(),
                        "bookingCode", booking.getBookingCode()
                ),
                ip,
                userAgent
        );

        return mappers.toPayment(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> listAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
                paymentRepository.findAllByOrderByCreatedAtDesc(pageable)
                        .map(mappers::toPayment));
    }

    private Payment findOrThrow(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment", id));
    }

    private void approvePayment(Payment payment, Booking booking) {
        payment.setPaidAt(Instant.now());
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        reminderService.scheduleForBooking(booking.getId());
        eventPublisher.publishEvent(new BookingConfirmedEvent(this, booking));
        eventPublisher.publishEvent(new PaymentCompletedEvent(this, payment, booking));
    }

    private void rejectPayment(Payment payment, Booking booking) {
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        List<UUID> seatIds = booking.getSeats().stream()
                .map(Seat::getId)
                .toList();
        seatService.freeSeats(seatIds);
        reminderService.cancelForBooking(booking.getId());
        eventPublisher.publishEvent(new PaymentFailedEvent(this, payment, booking));
    }
}
