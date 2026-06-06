package com.auvan.backend.booking.controller;

import com.auvan.backend.booking.dto.CreateBookingRequest;
import com.auvan.backend.booking.dto.BookingResponse;
import com.auvan.backend.payment.dto.PaymentResponse;
import com.auvan.backend.route.dto.RouteResponse;
import com.auvan.backend.timeslot.dto.TimeslotResponse;
import com.auvan.backend.user.entity.User;
import com.auvan.backend.booking.enums.BookingStatus;
import com.auvan.backend.payment.enums.PaymentMethod;
import com.auvan.backend.payment.enums.PaymentStatus;
import com.auvan.backend.route.enums.RouteStatus;
import com.auvan.backend.booking.enums.SourceChannel;
import com.auvan.backend.timeslot.enums.TimeslotStatus;
import com.auvan.backend.shared.exception.ResourceNotFoundException;
import com.auvan.backend.shared.exception.UnauthorizedException;
import com.auvan.backend.shared.security.CustomUserDetails;
import com.auvan.backend.booking.service.BookingService;
import com.auvan.backend.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiffBookingControllerTest {

    @Mock private BookingService bookingService;
    @Mock private PaymentService paymentService;

    @InjectMocks private LiffBookingController controller;

    @Test
    void createBookingReturnsCreated() {
        UUID userId = UUID.randomUUID();
        CustomUserDetails principal = principal(userId, false);
        CreateBookingRequest request = sampleCreateBookingRequest();

        when(bookingService.createBooking(eq(userId), any(CreateBookingRequest.class)))
                .thenReturn(sampleBookingResponse(userId));

        var response = controller.createBooking(principal, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
    }

    @Test
    void createBookingWithoutPrincipalThrowsUnauthorized() {
        assertThatThrownBy(() -> controller.createBooking(null, sampleCreateBookingRequest()))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getByIdPropagatesNotFound() {
        UUID userId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        CustomUserDetails principal = principal(userId, false);

        when(bookingService.getById(bookingId, userId))
                .thenThrow(ResourceNotFoundException.of("Booking", bookingId));

        assertThatThrownBy(() -> controller.getById(principal, bookingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private CreateBookingRequest sampleCreateBookingRequest() {
        return new CreateBookingRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(UUID.randomUUID()),
                "Alice",
                "0812345678",
                "ABAC Gate",
                PaymentMethod.PROMPTPAY,
                SourceChannel.LIFF,
                "idem-1"
        );
    }

    private CustomUserDetails principal(UUID userId, boolean admin) {
        User user = new User();
        user.setId(userId);
        user.setName("Tester");
        user.setAdmin(admin);
        return new CustomUserDetails(user);
    }

    private BookingResponse sampleBookingResponse(UUID userId) {
        UUID routeId = UUID.randomUUID();
        UUID timeslotId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();

        RouteResponse route = new RouteResponse(
                routeId,
                "ABAC",
                "BTS Udomsuk",
                "abac-bts",
                BigDecimal.valueOf(80),
                35,
                RouteStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );

        TimeslotResponse timeslot = new TimeslotResponse(
                timeslotId,
                routeId,
                "ABAC",
                "BTS Udomsuk",
                LocalDate.now(),
                LocalTime.of(9, 0),
                12,
                4,
                8,
                TimeslotStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );

        PaymentResponse payment = new PaymentResponse(
                UUID.randomUUID(),
                bookingId,
                BigDecimal.valueOf(80),
                PaymentMethod.PROMPTPAY,
                PaymentStatus.PENDING,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.now()
        );

        return new BookingResponse(
                bookingId,
                "AUV-260329-ABCDE",
                userId,
                route,
                timeslot,
                List.of(),
                1,
                "Alice",
                "0812345678",
                "ABAC Gate",
                BookingStatus.PENDING_PAYMENT,
                payment,
                Instant.now().plusSeconds(3600),
                SourceChannel.LIFF,
                null,
                BigDecimal.valueOf(80),
                Instant.now(),
                Instant.now()
        );
    }
}
