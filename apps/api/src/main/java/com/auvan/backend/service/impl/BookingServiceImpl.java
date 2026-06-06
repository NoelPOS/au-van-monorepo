package com.auvan.backend.service.impl;

import com.auvan.backend.mapper.EntityMappers;

import com.auvan.backend.dto.request.CreateBookingRequest;
import com.auvan.backend.dto.request.RescheduleRequest;
import com.auvan.backend.dto.request.UpdateBookingRequest;
import com.auvan.backend.dto.response.BookingResponse;
import com.auvan.backend.dto.response.PageResponse;
import com.auvan.backend.entity.Booking;
import com.auvan.backend.entity.IdempotencyKey;
import com.auvan.backend.entity.Payment;
import com.auvan.backend.entity.Route;
import com.auvan.backend.entity.Seat;
import com.auvan.backend.entity.Timeslot;
import com.auvan.backend.entity.User;
import com.auvan.backend.enums.BookingStatus;
import com.auvan.backend.enums.CancellationReason;
import com.auvan.backend.enums.IdempotencyStatus;
import com.auvan.backend.enums.PaymentMethod;
import com.auvan.backend.enums.PaymentStatus;
import com.auvan.backend.enums.RouteStatus;
import com.auvan.backend.enums.SourceChannel;
import com.auvan.backend.enums.TimeslotStatus;
import com.auvan.backend.event.BookingCancelledEvent;
import com.auvan.backend.event.BookingCreatedEvent;
import com.auvan.backend.exception.ConflictException;
import com.auvan.backend.exception.ForbiddenException;
import com.auvan.backend.exception.PaymentDeadlineException;
import com.auvan.backend.exception.ResourceNotFoundException;

import com.auvan.backend.repository.BookingRepository;
import com.auvan.backend.repository.PaymentRepository;
import com.auvan.backend.repository.RouteRepository;
import com.auvan.backend.repository.SeatRepository;
import com.auvan.backend.repository.TimeslotRepository;
import com.auvan.backend.repository.UserRepository;
import com.auvan.backend.service.BookingService;
import com.auvan.backend.service.IdempotencyService;
import com.auvan.backend.service.ReminderService;
import com.auvan.backend.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final String BOOKING_CREATE_SCOPE = "booking_create";
    private static final String BOOKING_RESCHEDULE_SCOPE = "booking_reschedule";

    @Value("${payment.deadline.minutes.before.departure:60}")
    private int paymentDeadlineMinutes;

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RouteRepository routeRepository;
    private final TimeslotRepository timeslotRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final SeatService seatService;
    private final ReminderService reminderService;
    private final IdempotencyService idempotencyService;
    private final EntityMappers mappers;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public BookingResponse createBooking(UUID userId, CreateBookingRequest request) {
        Optional<BookingResponse> replayResponse = findReplayResponse(
                userId,
                BOOKING_CREATE_SCOPE,
                request.idempotencyKey()
        );
        if (replayResponse.isPresent()) {
            return replayResponse.get();
        }

        IdempotencyKey idempotencyKey = startIdempotentRequest(
                userId,
                BOOKING_CREATE_SCOPE,
                request.idempotencyKey(),
                request
        );

        try {
            BookingResponse response = doCreateBooking(userId, request);
            completeIdempotentRequest(idempotencyKey, response, 201);
            return response;
        } catch (RuntimeException ex) {
            failIdempotentRequest(idempotencyKey, ex);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getById(UUID bookingId, UUID requestingUserId) {
        Booking booking = findOrThrow(bookingId);
        if (!booking.getUser().getId().equals(requestingUserId) && !isAdmin(requestingUserId)) {
            throw new ForbiddenException("You do not have access to this booking");
        }
        return mappers.toBooking(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(UUID userId) {
        return mappers.toBookingList(
                bookingRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> listAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
                bookingRepository.findAllByOrderByCreatedAtDesc(pageable)
                        .map(mappers::toBooking));
    }

    @Override
    @Transactional
    public BookingResponse update(UUID bookingId, UUID userId, UpdateBookingRequest request) {
        Booking booking = findOrThrow(bookingId);
        assertOwner(booking, userId);
        assertMutable(booking);

        if (StringUtils.hasText(request.passengerName())) {
            booking.setPassengerName(request.passengerName());
        }
        if (StringUtils.hasText(request.passengerPhone())) {
            booking.setPassengerPhone(request.passengerPhone());
        }
        if (StringUtils.hasText(request.pickupLocation())) {
            booking.setPickupLocation(request.pickupLocation());
        }

        return mappers.toBooking(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public void cancel(UUID bookingId, UUID requestingUserId, boolean isAdmin) {
        Booking booking = findOrThrow(bookingId);

        if (!isAdmin) {
            assertOwner(booking, requestingUserId);
        }

        assertMutable(booking);
        cancelBooking(booking, isAdmin ? CancellationReason.ADMIN_CANCELLED : CancellationReason.USER_CANCELLED);
    }

    @Override
    @Transactional
    public BookingResponse reschedule(UUID bookingId, UUID userId, RescheduleRequest request) {
        Booking original = findOrThrow(bookingId);
        assertOwner(original, userId);

        if (!isReschedulable(original)) {
            throw new ConflictException("Only confirmed or pending-payment bookings can be rescheduled");
        }

        Optional<BookingResponse> replayResponse = findReplayResponse(
                userId,
                BOOKING_RESCHEDULE_SCOPE,
                request.idempotencyKey()
        );
        if (replayResponse.isPresent()) {
            return replayResponse.get();
        }

        IdempotencyKey idempotencyKey = startIdempotentRequest(
                userId,
                BOOKING_RESCHEDULE_SCOPE,
                request.idempotencyKey(),
                request
        );

        PaymentMethod paymentMethod = original.getPayment() != null
                ? original.getPayment().getMethod() : PaymentMethod.CASH;

        CreateBookingRequest createRequest = new CreateBookingRequest(
                original.getRoute().getId(),
                request.timeslotId(),
                request.seatIds(),
                original.getPassengerName(),
                original.getPassengerPhone(),
                original.getPickupLocation(),
                paymentMethod,
                original.getSourceChannel(),
                request.idempotencyKey()
        );

        try {
            BookingResponse response = doCreateBooking(userId, createRequest, original);
            cancel(original.getId(), userId, false);
            completeIdempotentRequest(idempotencyKey, response, 200);
            return response;
        } catch (RuntimeException ex) {
            failIdempotentRequest(idempotencyKey, ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public int expireUnpaidBookings(int limit) {
        var pageable = PageRequest.of(0, limit);
        List<Booking> expired = bookingRepository.findExpiredUnpaidBookings(Instant.now(), pageable);

        for (Booking booking : expired) {
            cancelBooking(booking, CancellationReason.PAYMENT_DEADLINE_EXCEEDED);
            log.info("Expired unpaid booking {} ({})", booking.getBookingCode(), booking.getId());
        }

        return expired.size();
    }

    private BookingResponse doCreateBooking(UUID userId, CreateBookingRequest request) {
        return doCreateBooking(userId, request, null);
    }

    private BookingResponse doCreateBooking(UUID userId, CreateBookingRequest request, Booking rescheduledFromBooking) {
        Route route = findActiveRoute(request.routeId());
        Timeslot timeslot = findActiveTimeslot(request.timeslotId());
        validateTimeslotBelongsToRoute(route, timeslot);
        ensureNoOverlap(
                userId,
                timeslot.getId(),
                rescheduledFromBooking != null ? rescheduledFromBooking.getId() : null
        );
        ensurePaymentWindowOpen(request.paymentMethod(), timeslot);
        reserveSeats(timeslot.getId(), request.seatIds(), userId);

        List<Seat> seats = seatRepository.findAllById(request.seatIds());
        Instant paymentDueAt = computePaymentDueAt(timeslot);
        Booking booking = buildBookingEntity(userId, request, route, timeslot, seats, paymentDueAt);
        booking.setRescheduledFromBooking(rescheduledFromBooking);
        booking = bookingRepository.save(booking);

        Payment payment = createPaymentRecord(booking, userId, route.getPrice(), seats.size(), request.paymentMethod());
        booking.setPayment(payment);
        booking.setTotalPrice(payment.getAmount());
        booking = bookingRepository.save(booking);

        publishBookingCreated(booking);

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            reminderService.scheduleForBooking(booking.getId());
        }

        return mappers.toBooking(booking);
    }

    private Optional<BookingResponse> findReplayResponse(UUID userId, String scope, String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return Optional.empty();
        }

        return idempotencyService.find(userId, scope, idempotencyKey)
                .map(record -> {
                    if (record.getStatus() == IdempotencyStatus.IN_PROGRESS) {
                        throw new ConflictException("This request is already being processed", "DUPLICATE_REQUEST");
                    }
                    if (record.getStatus() == IdempotencyStatus.COMPLETED && record.getResponseData() != null) {
                        return mappers.toBooking(
                                bookingRepository.findById(UUID.fromString(record.getResponseData().toString()))
                                        .orElseThrow(() -> ResourceNotFoundException.of("Booking", record.getResponseData())));
                    }
                    return null;
                });
    }

    private IdempotencyKey startIdempotentRequest(UUID userId, String scope, String key, Object requestBody) {
        if (!StringUtils.hasText(key)) {
            return null;
        }

        return idempotencyService.startRequest(
                userId,
                scope,
                key,
                requestBody
        );
    }

    private void completeIdempotentRequest(IdempotencyKey idempotencyKey, BookingResponse response, int statusCode) {
        if (idempotencyKey != null) {
            idempotencyService.completeRequest(idempotencyKey.getId(), response.id().toString(), statusCode);
        }
    }

    private void failIdempotentRequest(IdempotencyKey idempotencyKey, RuntimeException ex) {
        if (idempotencyKey != null) {
            idempotencyService.failRequest(idempotencyKey.getId(), ex.getMessage());
        }
    }

    private Route findActiveRoute(UUID routeId) {
        return routeRepository.findById(routeId)
                .filter(route -> route.getStatus() == RouteStatus.ACTIVE)
                .orElseThrow(() -> ResourceNotFoundException.of("Route", routeId));
    }

    private Timeslot findActiveTimeslot(UUID timeslotId) {
        return timeslotRepository.findById(timeslotId)
                .filter(timeslot -> timeslot.getStatus() == TimeslotStatus.ACTIVE)
                .orElseThrow(() -> ResourceNotFoundException.of("Timeslot", timeslotId));
    }

    private void validateTimeslotBelongsToRoute(Route route, Timeslot timeslot) {
        if (!timeslot.getRoute().getId().equals(route.getId())) {
            throw new ConflictException("Timeslot does not belong to the selected route");
        }
    }

    private void ensureNoOverlap(UUID userId, UUID timeslotId, UUID excludedBookingId) {
        if (!bookingRepository.findActiveBookingsForUserOnTimeslot(userId, timeslotId, excludedBookingId).isEmpty()) {
            throw new ConflictException("You already have an active booking on this timeslot", "BOOKING_OVERLAP");
        }
    }

    private void ensurePaymentWindowOpen(PaymentMethod paymentMethod, Timeslot timeslot) {
        if (paymentMethod != PaymentMethod.CASH && Instant.now().isAfter(computePaymentDueAt(timeslot))) {
            throw new PaymentDeadlineException("The payment window for this timeslot has closed");
        }
    }

    private void reserveSeats(UUID timeslotId, List<UUID> seatIds, UUID userId) {
        seatService.lockSeats(timeslotId, seatIds, userId);
        seatService.confirmSeats(seatIds, userId);
    }

    private Payment createPaymentRecord(
            Booking booking,
            UUID userId,
            BigDecimal routePrice,
            int seatCount,
            PaymentMethod paymentMethod
    ) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setUser(userRepository.getReferenceById(userId));
        payment.setAmount(routePrice.multiply(BigDecimal.valueOf(seatCount)));
        payment.setMethod(paymentMethod);

        if (paymentMethod == PaymentMethod.CASH) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(Instant.now());
        } else {
            payment.setStatus(PaymentStatus.PENDING);
        }

        return paymentRepository.save(payment);
    }

    private void publishBookingCreated(Booking booking) {
        eventPublisher.publishEvent(new BookingCreatedEvent(this, booking));
    }

    private void cancelBooking(Booking booking, CancellationReason reason) {
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        seatService.freeSeats(getSeatIds(booking));
        reminderService.cancelForBooking(booking.getId());
        eventPublisher.publishEvent(new BookingCancelledEvent(this, booking, reason));
    }

    private List<UUID> getSeatIds(Booking booking) {
        return booking.getSeats().stream().map(Seat::getId).toList();
    }

    private boolean isReschedulable(Booking booking) {
        return booking.getStatus() == BookingStatus.CONFIRMED
                || booking.getStatus() == BookingStatus.PENDING_PAYMENT;
    }

    private Booking buildBookingEntity(UUID userId, CreateBookingRequest request) {
        Route route = routeRepository.findById(request.routeId())
                .orElseThrow(() -> ResourceNotFoundException.of("Route", request.routeId()));
        Timeslot timeslot = timeslotRepository.findById(request.timeslotId())
                .orElseThrow(() -> ResourceNotFoundException.of("Timeslot", request.timeslotId()));
        List<Seat> seats = seatRepository.findAllById(request.seatIds());
        return buildBookingEntity(userId, request, route, timeslot, seats, computePaymentDueAt(timeslot));
    }

    private Booking buildBookingEntity(
            UUID userId,
            CreateBookingRequest request,
            Route route,
            Timeslot timeslot,
            List<Seat> seats,
            Instant paymentDueAt
    ) {
        Booking booking = new Booking();
        booking.setUser(userRepository.getReferenceById(userId));
        booking.setRoute(route);
        booking.setTimeslot(timeslot);
        booking.setSeats(seats);
        booking.setPassengers(seats.size());
        booking.setPassengerName(request.passengerName());
        booking.setPassengerPhone(request.passengerPhone());
        booking.setPickupLocation(request.pickupLocation());
        booking.setStatus(initialStatusFor(request.paymentMethod()));
        booking.setPaymentDueAt(paymentDueAt);
        booking.setSourceChannel(request.sourceChannel() != null ? request.sourceChannel() : SourceChannel.LIFF);
        booking.setBookingCode(generateUniqueBookingCode());
        booking.setTotalPrice(route.getPrice().multiply(BigDecimal.valueOf(seats.size())));
        return booking;
    }

    private BookingStatus initialStatusFor(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.CASH
                ? BookingStatus.CONFIRMED
                : BookingStatus.PENDING_PAYMENT;
    }

    private Instant computePaymentDueAt(Timeslot timeslot) {
        return LocalDateTime.of(timeslot.getDate(), timeslot.getTime())
                .minusMinutes(paymentDeadlineMinutes)
                .toInstant(ZoneOffset.UTC);
    }

    private String generateUniqueBookingCode() {
        String datePart = LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));

        for (int attempt = 0; attempt < 10; attempt++) {
            String code = "AUV-" + datePart + "-" + RandomStringUtils.secure().nextAlphanumeric(5).toUpperCase();
            if (!bookingRepository.existsByBookingCode(code)) {
                return code;
            }
        }

        throw new IllegalStateException("Could not generate unique booking code after 10 attempts");
    }

    private Booking findOrThrow(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", id));
    }

    private void assertOwner(Booking booking, UUID userId) {
        if (!booking.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this booking");
        }
    }

    private boolean isAdmin(UUID userId) {
        return userRepository.findById(userId)
                .map(User::isAdmin)
                .orElse(false);
    }

    private void assertMutable(Booking booking) {
        if (booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new ConflictException(
                    "Booking is " + booking.getStatus().name().toLowerCase() + " and cannot be modified"
            );
        }
    }
}
