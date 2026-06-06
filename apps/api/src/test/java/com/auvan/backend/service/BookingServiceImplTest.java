package com.auvan.backend.service;

import com.auvan.backend.mapper.EntityMappers;

import com.auvan.backend.dto.request.CreateBookingRequest;
import com.auvan.backend.dto.request.RescheduleRequest;
import com.auvan.backend.dto.response.BookingResponse;
import com.auvan.backend.entity.Booking;
import com.auvan.backend.entity.IdempotencyKey;
import com.auvan.backend.entity.Payment;
import com.auvan.backend.entity.Route;
import com.auvan.backend.entity.Seat;
import com.auvan.backend.entity.Timeslot;
import com.auvan.backend.entity.User;
import com.auvan.backend.enums.BookingStatus;
import com.auvan.backend.enums.IdempotencyStatus;
import com.auvan.backend.enums.PaymentMethod;
import com.auvan.backend.enums.RouteStatus;
import com.auvan.backend.enums.SourceChannel;
import com.auvan.backend.enums.TimeslotStatus;
import com.auvan.backend.event.BookingCreatedEvent;
import com.auvan.backend.exception.ConflictException;
import com.auvan.backend.exception.SeatLockException;

import com.auvan.backend.repository.BookingRepository;
import com.auvan.backend.repository.PaymentRepository;
import com.auvan.backend.repository.RouteRepository;
import com.auvan.backend.repository.SeatRepository;
import com.auvan.backend.repository.TimeslotRepository;
import com.auvan.backend.repository.UserRepository;
import com.auvan.backend.service.impl.BookingServiceImpl;
import com.auvan.backend.service.helper.BookingCodeGenerator;
import com.auvan.backend.service.helper.BookingIdempotencyHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private RouteRepository routeRepository;
    @Mock private TimeslotRepository timeslotRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private UserRepository userRepository;
    @Mock private SeatService seatService;
    @Mock private ReminderService reminderService;
    @Mock private IdempotencyService idempotencyService;
    @Mock private EntityMappers mappers;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private BookingServiceImpl bookingService;

    private UUID userId;
    private UUID routeId;
    private UUID timeslotId;
    private UUID seatId;
    private Route route;
    private Timeslot timeslot;
    private User user;
    private Seat seat;

    @BeforeEach
    void setUp() {
        setField(
                bookingService,
                "idempotency",
                new BookingIdempotencyHelper(idempotencyService, bookingRepository, mappers)
        );
        setField(
                bookingService,
                "bookingCodeGenerator",
                new BookingCodeGenerator(bookingRepository)
        );
        setField(bookingService, "paymentDeadlineMinutes", 60);

        userId = UUID.randomUUID();
        routeId = UUID.randomUUID();
        timeslotId = UUID.randomUUID();
        seatId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setName("Alice");

        route = new Route();
        route.setId(routeId);
        route.setFromLocation("ABAC");
        route.setToLocation("BTS Udomsuk");
        route.setPrice(BigDecimal.valueOf(80));
        route.setStatus(RouteStatus.ACTIVE);

        timeslot = new Timeslot();
        timeslot.setId(timeslotId);
        timeslot.setRoute(route);
        timeslot.setDate(LocalDate.now().plusDays(1));
        timeslot.setTime(LocalTime.of(9, 0));
        timeslot.setStatus(TimeslotStatus.ACTIVE);
        timeslot.setTotalSeats(12);

        seat = new Seat();
        seat.setId(seatId);
        seat.setTimeslot(timeslot);
        seat.setSeatNumber(1);
    }

    @Test
    void createBookingHappyPathPublishesEvent() {
        CreateBookingRequest request = new CreateBookingRequest(
                routeId,
                timeslotId,
                List.of(seatId),
                "Alice",
                "0812345678",
                "ABAC Gate",
                PaymentMethod.PROMPTPAY,
                SourceChannel.LIFF,
                null
        );

        when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
        when(timeslotRepository.findById(timeslotId)).thenReturn(Optional.of(timeslot));
        when(bookingRepository.findActiveBookingsForUserOnTimeslot(userId, timeslotId, null)).thenReturn(List.of());
        when(seatRepository.findAllById(List.of(seatId))).thenReturn(List.of(seat));
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(bookingRepository.existsByBookingCode(anyString())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            if (b.getId() == null) {
                b.setId(UUID.randomUUID());
            }
            return b;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        BookingResponse mapped = new BookingResponse(
                UUID.randomUUID(), "AUV-260329-ABCDE", userId, null, null, List.of(),
                1, "Alice", "0812345678", "ABAC Gate",
                BookingStatus.PENDING_PAYMENT, null, Instant.now().plusSeconds(3600),
                SourceChannel.LIFF, null, BigDecimal.valueOf(80), Instant.now(), Instant.now()
        );
        when(mappers.toBooking(any(Booking.class))).thenReturn(mapped);

        BookingResponse response = bookingService.createBooking(userId, request);

        assertThat(response).isEqualTo(mapped);
        verify(seatService).lockSeats(timeslotId, List.of(seatId), userId);
        verify(seatService).confirmSeats(List.of(seatId), userId);
        verify(eventPublisher).publishEvent(any(BookingCreatedEvent.class));
        verify(reminderService, never()).scheduleForBooking(any());
    }

    @Test
    void createBookingWithInProgressIdempotencyKeyThrowsConflict() {
        CreateBookingRequest request = new CreateBookingRequest(
                routeId, timeslotId, List.of(seatId), "Alice", "0812345678",
                "ABAC Gate", PaymentMethod.CASH, SourceChannel.LIFF, "idem-key"
        );

        IdempotencyKey key = new IdempotencyKey();
        key.setStatus(IdempotencyStatus.IN_PROGRESS);
        when(idempotencyService.find(userId, "booking_create", "idem-key")).thenReturn(Optional.of(key));

        assertThatThrownBy(() -> bookingService.createBooking(userId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already being processed");
    }

    @Test
    void createBookingWhenSeatUnavailableThrowsSeatLockException() {
        CreateBookingRequest request = new CreateBookingRequest(
                routeId,
                timeslotId,
                List.of(seatId),
                "Alice",
                "0812345678",
                "ABAC Gate",
                PaymentMethod.PROMPTPAY,
                SourceChannel.LIFF,
                null
        );

        when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
        when(timeslotRepository.findById(timeslotId)).thenReturn(Optional.of(timeslot));
        when(bookingRepository.findActiveBookingsForUserOnTimeslot(userId, timeslotId, null)).thenReturn(List.of());
        when(seatService.lockSeats(timeslotId, List.of(seatId), userId))
                .thenThrow(new SeatLockException("Seat is not available"));

        assertThatThrownBy(() -> bookingService.createBooking(userId, request))
                .isInstanceOf(SeatLockException.class);

        verify(seatService, never()).confirmSeats(any(), any());
    }

    @Test
    void createBookingWithOverlappingBookingThrowsConflict() {
        CreateBookingRequest request = new CreateBookingRequest(
                routeId,
                timeslotId,
                List.of(seatId),
                "Alice",
                "0812345678",
                "ABAC Gate",
                PaymentMethod.PROMPTPAY,
                SourceChannel.LIFF,
                null
        );

        Booking active = new Booking();
        active.setId(UUID.randomUUID());

        when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
        when(timeslotRepository.findById(timeslotId)).thenReturn(Optional.of(timeslot));
        when(bookingRepository.findActiveBookingsForUserOnTimeslot(userId, timeslotId, null))
                .thenReturn(List.of(active));

        assertThatThrownBy(() -> bookingService.createBooking(userId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already have an active booking");

        verify(seatService, never()).lockSeats(any(), any(), any());
    }

    @Test
    void rescheduleCreatesNewBookingWithPaymentAndCancelsOriginal() {
        UUID originalBookingId = UUID.randomUUID();
        UUID nextTimeslotId = UUID.randomUUID();
        UUID nextSeatId = UUID.randomUUID();

        Timeslot nextTimeslot = new Timeslot();
        nextTimeslot.setId(nextTimeslotId);
        nextTimeslot.setRoute(route);
        nextTimeslot.setDate(LocalDate.now().plusDays(2));
        nextTimeslot.setTime(LocalTime.of(11, 0));
        nextTimeslot.setStatus(TimeslotStatus.ACTIVE);
        nextTimeslot.setTotalSeats(12);

        Seat nextSeat = new Seat();
        nextSeat.setId(nextSeatId);
        nextSeat.setTimeslot(nextTimeslot);
        nextSeat.setSeatNumber(2);

        Payment originalPayment = new Payment();
        originalPayment.setMethod(PaymentMethod.PROMPTPAY);

        Booking originalBooking = new Booking();
        originalBooking.setId(originalBookingId);
        originalBooking.setUser(user);
        originalBooking.setRoute(route);
        originalBooking.setTimeslot(timeslot);
        originalBooking.setSeats(List.of(seat));
        originalBooking.setPassengerName("Alice");
        originalBooking.setPassengerPhone("0812345678");
        originalBooking.setPickupLocation("ABAC Gate");
        originalBooking.setStatus(BookingStatus.PENDING_PAYMENT);
        originalBooking.setPayment(originalPayment);

        RescheduleRequest request = new RescheduleRequest(
                nextTimeslotId,
                List.of(nextSeatId),
                "reschedule-idem-key"
        );

        when(bookingRepository.findById(originalBookingId)).thenReturn(Optional.of(originalBooking));
        when(idempotencyService.find(userId, "booking_reschedule", "reschedule-idem-key"))
                .thenReturn(Optional.empty());

        IdempotencyKey key = new IdempotencyKey();
        key.setId(UUID.randomUUID());
        when(idempotencyService.startRequest(eq(userId), eq("booking_reschedule"), eq("reschedule-idem-key"), eq(request)))
                .thenReturn(key);

        when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
        when(timeslotRepository.findById(nextTimeslotId)).thenReturn(Optional.of(nextTimeslot));
        when(bookingRepository.findActiveBookingsForUserOnTimeslot(userId, nextTimeslotId, originalBookingId))
                .thenReturn(List.of());
        when(seatRepository.findAllById(List.of(nextSeatId))).thenReturn(List.of(nextSeat));
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(bookingRepository.existsByBookingCode(anyString())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            if (b.getId() == null) {
                b.setId(UUID.randomUUID());
            }
            return b;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        BookingResponse mapped = new BookingResponse(
                UUID.randomUUID(), "AUV-260329-ABCDE", userId, null, null, List.of(),
                1, "Alice", "0812345678", "ABAC Gate",
                BookingStatus.PENDING_PAYMENT, null, Instant.now().plusSeconds(3600),
                SourceChannel.LIFF, originalBookingId, BigDecimal.valueOf(80), Instant.now(), Instant.now()
        );
        when(mappers.toBooking(any(Booking.class))).thenReturn(mapped);

        BookingResponse response = bookingService.reschedule(originalBookingId, userId, request);

        assertThat(response).isEqualTo(mapped);
        verify(idempotencyService).find(userId, "booking_reschedule", "reschedule-idem-key");
        verify(idempotencyService).startRequest(userId, "booking_reschedule", "reschedule-idem-key", request);
        verify(bookingRepository).findActiveBookingsForUserOnTimeslot(userId, nextTimeslotId, originalBookingId);
        verify(seatService).lockSeats(nextTimeslotId, List.of(nextSeatId), userId);
        verify(seatService).confirmSeats(List.of(nextSeatId), userId);
        verify(paymentRepository).save(any(Payment.class));
        verify(seatService).freeSeats(List.of(seatId));
        verify(idempotencyService).completeRequest(key.getId(), response.id().toString(), 200);
        verify(reminderService, never()).scheduleForBooking(any());
    }
}
