package com.auvan.backend.booking.service;

import com.auvan.backend.shared.mapper.EntityMappers;

import com.auvan.backend.booking.dto.CreateBookingRequest;
import com.auvan.backend.booking.dto.RescheduleRequest;
import com.auvan.backend.booking.dto.UpdateBookingRequest;
import com.auvan.backend.booking.dto.BookingResponse;
import com.auvan.backend.shared.dto.PageResponse;
import com.auvan.backend.booking.Booking;
import com.auvan.backend.payment.Payment;
import com.auvan.backend.route.Route;
import com.auvan.backend.seat.Seat;
import com.auvan.backend.timeslot.Timeslot;
import com.auvan.backend.user.User;
import com.auvan.backend.booking.BookingStatus;
import com.auvan.backend.booking.CancellationReason;
import com.auvan.backend.payment.PaymentMethod;
import com.auvan.backend.payment.PaymentStatus;
import com.auvan.backend.route.RouteStatus;
import com.auvan.backend.booking.SourceChannel;
import com.auvan.backend.timeslot.TimeslotStatus;
import com.auvan.backend.booking.event.BookingCancelledEvent;
import com.auvan.backend.booking.event.BookingCreatedEvent;
import com.auvan.backend.shared.exception.ConflictException;
import com.auvan.backend.shared.exception.ForbiddenException;
import com.auvan.backend.shared.exception.PaymentDeadlineException;
import com.auvan.backend.shared.exception.ResourceNotFoundException;

import com.auvan.backend.booking.BookingRepository;
import com.auvan.backend.payment.PaymentRepository;
import com.auvan.backend.route.RouteRepository;
import com.auvan.backend.seat.SeatRepository;
import com.auvan.backend.timeslot.TimeslotRepository;
import com.auvan.backend.user.UserRepository;
import com.auvan.backend.booking.service.BookingService;
import com.auvan.backend.reminder.service.ReminderService;
import com.auvan.backend.seat.service.SeatService;
import com.auvan.backend.booking.helper.BookingCodeGenerator;
import com.auvan.backend.booking.helper.BookingIdempotencyHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
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
    private final EntityMappers mappers;
    private final ApplicationEventPublisher eventPublisher;
    private final BookingIdempotencyHelper idempotency;
    private final BookingCodeGenerator bookingCodeGenerator;

    @Override
    @Transactional
    public BookingResponse createBooking(UUID userId, CreateBookingRequest request) {
        return idempotency.run(
                userId,
                BOOKING_CREATE_SCOPE,
                request.idempotencyKey(),
                request,
                201,
                () -> doCreateBooking(userId, request)
        );
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

        return idempotency.run(
                userId,
                BOOKING_RESCHEDULE_SCOPE,
                request.idempotencyKey(),
                request,
                200,
                () -> {
                    BookingResponse response = doCreateBooking(userId, createRequest, original);
                    cancel(original.getId(), userId, false);
                    return response;
                }
        );
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
        booking.setBookingCode(bookingCodeGenerator.generate());
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
