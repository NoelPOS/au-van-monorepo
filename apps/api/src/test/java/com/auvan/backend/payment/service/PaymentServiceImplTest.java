package com.auvan.backend.payment.service;

import com.auvan.backend.payment.dto.ReviewPaymentRequest;
import com.auvan.backend.payment.dto.PaymentResponse;
import com.auvan.backend.booking.Booking;
import com.auvan.backend.payment.Payment;
import com.auvan.backend.seat.Seat;
import com.auvan.backend.user.User;
import com.auvan.backend.booking.BookingStatus;
import com.auvan.backend.payment.PaymentMethod;
import com.auvan.backend.payment.PaymentStatus;
import com.auvan.backend.booking.event.BookingConfirmedEvent;
import com.auvan.backend.payment.event.PaymentCompletedEvent;
import com.auvan.backend.payment.event.PaymentFailedEvent;
import com.auvan.backend.shared.mapper.EntityMappers;
import com.auvan.backend.audit.service.AuditLogService;
import com.auvan.backend.booking.BookingRepository;
import com.auvan.backend.payment.PaymentRepository;
import com.auvan.backend.payment.service.PaymentServiceImpl;
import com.auvan.backend.reminder.service.ReminderService;
import com.auvan.backend.seat.service.SeatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private SeatService seatService;
    @Mock private ReminderService reminderService;
    @Mock private AuditLogService auditLogService;
    @Mock private EntityMappers mappers;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private PaymentServiceImpl paymentService;

    @Test
    void reviewPaymentApproveConfirmsBookingAndSchedulesReminder() {
        UUID paymentId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Payment payment = samplePayment(paymentId, PaymentStatus.PENDING_REVIEW);
        Booking booking = payment.getBooking();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mappers.toPayment(any(Payment.class))).thenReturn(new PaymentResponse(
                paymentId, booking.getId(), BigDecimal.valueOf(80), PaymentMethod.PROMPTPAY,
                PaymentStatus.COMPLETED, "TX123", null, null, null, adminId,
                Instant.now(), "ok", Instant.now(), null, Instant.now()
        ));

        PaymentResponse response = paymentService.reviewPayment(
                paymentId,
                adminId,
                new ReviewPaymentRequest(PaymentStatus.COMPLETED, "TX123", "ok"),
                "127.0.0.1",
                "JUnit"
        );

        assertThat(response.status()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(reminderService).scheduleForBooking(booking.getId());
        verify(eventPublisher).publishEvent(any(BookingConfirmedEvent.class));
        verify(eventPublisher).publishEvent(any(PaymentCompletedEvent.class));
        verify(seatService, never()).freeSeats(any());
    }

    @Test
    void reviewPaymentRejectCancelsBookingAndFreesSeats() {
        UUID paymentId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Payment payment = samplePayment(paymentId, PaymentStatus.PENDING_REVIEW);
        Booking booking = payment.getBooking();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mappers.toPayment(any(Payment.class))).thenReturn(new PaymentResponse(
                paymentId, booking.getId(), BigDecimal.valueOf(80), PaymentMethod.PROMPTPAY,
                PaymentStatus.FAILED, null, null, null, null, adminId,
                Instant.now(), "rejected", null, null, Instant.now()
        ));

        PaymentResponse response = paymentService.reviewPayment(
                paymentId,
                adminId,
                new ReviewPaymentRequest(PaymentStatus.FAILED, null, "rejected"),
                "127.0.0.1",
                "JUnit"
        );

        assertThat(response.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(seatService).freeSeats(any());
        verify(reminderService).cancelForBooking(booking.getId());
        verify(eventPublisher).publishEvent(any(PaymentFailedEvent.class));
    }

    private Payment samplePayment(UUID paymentId, PaymentStatus status) {
        UUID bookingId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();

        Seat seat = new Seat();
        seat.setId(seatId);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setBookingCode("AUV-260329-ABCDE");
        booking.setStatus(BookingStatus.PAYMENT_UNDER_REVIEW);
        booking.setSeats(List.of(seat));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Alice");

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setBooking(booking);
        payment.setUser(user);
        payment.setAmount(BigDecimal.valueOf(80));
        payment.setMethod(PaymentMethod.PROMPTPAY);
        payment.setStatus(status);
        return payment;
    }
}
