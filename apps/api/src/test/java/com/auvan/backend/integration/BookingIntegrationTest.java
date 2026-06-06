package com.auvan.backend.integration;

import com.auvan.backend.booking.Booking;
import com.auvan.backend.payment.Payment;
import com.auvan.backend.route.Route;
import com.auvan.backend.seat.Seat;
import com.auvan.backend.timeslot.Timeslot;
import com.auvan.backend.user.User;
import com.auvan.backend.auth.AuthProvider;
import com.auvan.backend.booking.BookingStatus;
import com.auvan.backend.payment.PaymentStatus;
import com.auvan.backend.route.RouteStatus;
import com.auvan.backend.seat.SeatStatus;
import com.auvan.backend.timeslot.TimeslotStatus;
import com.auvan.backend.booking.BookingRepository;
import com.auvan.backend.payment.PaymentRepository;
import com.auvan.backend.route.RouteRepository;
import com.auvan.backend.seat.SeatRepository;
import com.auvan.backend.timeslot.TimeslotRepository;
import com.auvan.backend.user.UserRepository;
import com.auvan.backend.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BookingIntegrationTest extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private UserRepository userRepository;
    @Autowired private RouteRepository routeRepository;
    @Autowired private TimeslotRepository timeslotRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void cleanDatabase() {
        resetDatabase();
    }

    @Test
    @SuppressWarnings("unchecked")
    void fullBookingFlowCreateThenApprovePayment() {
        String userToken = registerAndGetToken("booking.user@auvan.app");

        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin@auvan.app");
        admin.setAuthProvider(AuthProvider.LOCAL);
        admin.setAdmin(true);
        admin = userRepository.save(admin);
        String adminToken = jwtTokenProvider.generateToken(admin.getId(), admin.getEmail(), true);

        Route route = new Route();
        route.setFromLocation("ABAC");
        route.setToLocation("BTS Udomsuk");
        route.setSlug("abac-bts-udomsuk");
        route.setPrice(BigDecimal.valueOf(80));
        route.setDurationMinutes(35);
        route.setStatus(RouteStatus.ACTIVE);
        route = routeRepository.save(route);

        Timeslot timeslot = new Timeslot();
        timeslot.setRoute(route);
        timeslot.setDate(LocalDate.now().plusDays(1));
        timeslot.setTime(LocalTime.of(9, 0));
        timeslot.setTotalSeats(12);
        timeslot.setBookedSeats(0);
        timeslot.setStatus(TimeslotStatus.ACTIVE);
        timeslot = timeslotRepository.save(timeslot);

        Seat seat = new Seat();
        seat.setTimeslot(timeslot);
        seat.setSeatNumber(1);
        seat.setLabel(Seat.buildLabel(1));
        seat.setStatus(SeatStatus.AVAILABLE);
        seat = seatRepository.save(seat);

        Map<String, Object> createBookingRequest = Map.of(
                "routeId", route.getId(),
                "timeslotId", timeslot.getId(),
                "seatIds", List.of(seat.getId()),
                "passengerName", "Booking User",
                "passengerPhone", "0812345678",
                "pickupLocation", "ABAC Gate",
                "paymentMethod", "PROMPTPAY",
                "sourceChannel", "LIFF",
                "idempotencyKey", "int-booking-flow-1"
        );

        ResponseEntity<Map> createBookingResponse = restTemplate.exchange(
                "/api/liff/bookings",
                HttpMethod.POST,
                authorizedJson(userToken, createBookingRequest),
                Map.class
        );
        assertThat(createBookingResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Map<String, Object> bookingData = (Map<String, Object>) createBookingResponse.getBody().get("data");
        UUID bookingId = UUID.fromString((String) bookingData.get("id"));

        Payment payment = paymentRepository.findByBookingId(bookingId).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        Map<String, Object> reviewRequest = Map.of(
                "status", "COMPLETED",
                "transactionId", "TX-INTEGRATION-123",
                "reviewNote", "Approved"
        );

        ResponseEntity<Map> reviewResponse = restTemplate.exchange(
                "/api/admin/payments/" + payment.getId() + "/review",
                HttpMethod.PUT,
                authorizedJson(adminToken, reviewRequest),
                Map.class
        );
        assertThat(reviewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Booking refreshedBooking = bookingRepository.findById(bookingId).orElseThrow();
        Seat refreshedSeat = seatRepository.findById(seat.getId()).orElseThrow();
        Payment refreshedPayment = paymentRepository.findById(payment.getId()).orElseThrow();

        assertThat(refreshedBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(refreshedSeat.getStatus()).isEqualTo(SeatStatus.BOOKED);
        assertThat(refreshedPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    @SuppressWarnings("unchecked")
    void rescheduleCreatesReplacementBookingAndCancelsOriginal() {
        String userToken = registerAndGetToken("reschedule.user@auvan.app");

        Route route = new Route();
        route.setFromLocation("ABAC");
        route.setToLocation("BTS Udomsuk");
        route.setSlug("abac-bts-udomsuk");
        route.setPrice(BigDecimal.valueOf(80));
        route.setDurationMinutes(35);
        route.setStatus(RouteStatus.ACTIVE);
        route = routeRepository.save(route);

        Timeslot firstTimeslot = new Timeslot();
        firstTimeslot.setRoute(route);
        firstTimeslot.setDate(LocalDate.now().plusDays(1));
        firstTimeslot.setTime(LocalTime.of(9, 0));
        firstTimeslot.setTotalSeats(12);
        firstTimeslot.setBookedSeats(0);
        firstTimeslot.setStatus(TimeslotStatus.ACTIVE);
        firstTimeslot = timeslotRepository.save(firstTimeslot);

        Timeslot secondTimeslot = new Timeslot();
        secondTimeslot.setRoute(route);
        secondTimeslot.setDate(LocalDate.now().plusDays(2));
        secondTimeslot.setTime(LocalTime.of(11, 0));
        secondTimeslot.setTotalSeats(12);
        secondTimeslot.setBookedSeats(0);
        secondTimeslot.setStatus(TimeslotStatus.ACTIVE);
        secondTimeslot = timeslotRepository.save(secondTimeslot);

        Seat firstSeat = new Seat();
        firstSeat.setTimeslot(firstTimeslot);
        firstSeat.setSeatNumber(1);
        firstSeat.setLabel(Seat.buildLabel(1));
        firstSeat.setStatus(SeatStatus.AVAILABLE);
        firstSeat = seatRepository.save(firstSeat);

        Seat secondSeat = new Seat();
        secondSeat.setTimeslot(secondTimeslot);
        secondSeat.setSeatNumber(1);
        secondSeat.setLabel(Seat.buildLabel(1));
        secondSeat.setStatus(SeatStatus.AVAILABLE);
        secondSeat = seatRepository.save(secondSeat);

        Map<String, Object> createBookingRequest = Map.of(
                "routeId", route.getId(),
                "timeslotId", firstTimeslot.getId(),
                "seatIds", List.of(firstSeat.getId()),
                "passengerName", "Booking User",
                "passengerPhone", "0812345678",
                "pickupLocation", "ABAC Gate",
                "paymentMethod", "PROMPTPAY",
                "sourceChannel", "LIFF",
                "idempotencyKey", "int-reschedule-create"
        );

        ResponseEntity<Map> createBookingResponse = restTemplate.exchange(
                "/api/liff/bookings",
                HttpMethod.POST,
                authorizedJson(userToken, createBookingRequest),
                Map.class
        );
        assertThat(createBookingResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Map<String, Object> originalBookingData = (Map<String, Object>) createBookingResponse.getBody().get("data");
        UUID originalBookingId = UUID.fromString((String) originalBookingData.get("id"));

        Map<String, Object> rescheduleRequest = Map.of(
                "timeslotId", secondTimeslot.getId(),
                "seatIds", List.of(secondSeat.getId()),
                "idempotencyKey", "int-reschedule-flow-1"
        );

        ResponseEntity<Map> rescheduleResponse = restTemplate.exchange(
                "/api/liff/bookings/" + originalBookingId + "/reschedule",
                HttpMethod.POST,
                authorizedJson(userToken, rescheduleRequest),
                Map.class
        );
        assertThat(rescheduleResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, Object> replacementBookingData = (Map<String, Object>) rescheduleResponse.getBody().get("data");
        UUID replacementBookingId = UUID.fromString((String) replacementBookingData.get("id"));

        Booking originalBooking = bookingRepository.findById(originalBookingId).orElseThrow();
        Booking replacementBooking = bookingRepository.findById(replacementBookingId).orElseThrow();
        Payment replacementPayment = paymentRepository.findByBookingId(replacementBookingId).orElseThrow();

        assertThat(originalBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(replacementBooking.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(replacementBooking.getRescheduledFromBooking().getId()).isEqualTo(originalBookingId);
        assertThat(replacementBooking.getPayment()).isNotNull();
        assertThat(replacementPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(replacementPayment.getAmount()).isEqualByComparingTo("80");
    }

    @Test
    @SuppressWarnings("unchecked")
    void submitPaymentProofMovesBookingIntoReview() {
        String userToken = registerAndGetToken("proof.user@auvan.app");

        Route route = new Route();
        route.setFromLocation("ABAC");
        route.setToLocation("BTS Udomsuk");
        route.setSlug("abac-bts-udomsuk");
        route.setPrice(BigDecimal.valueOf(80));
        route.setDurationMinutes(35);
        route.setStatus(RouteStatus.ACTIVE);
        route = routeRepository.save(route);

        Timeslot timeslot = new Timeslot();
        timeslot.setRoute(route);
        timeslot.setDate(LocalDate.now().plusDays(1));
        timeslot.setTime(LocalTime.of(9, 0));
        timeslot.setTotalSeats(12);
        timeslot.setBookedSeats(0);
        timeslot.setStatus(TimeslotStatus.ACTIVE);
        timeslot = timeslotRepository.save(timeslot);

        Seat seat = new Seat();
        seat.setTimeslot(timeslot);
        seat.setSeatNumber(1);
        seat.setLabel(Seat.buildLabel(1));
        seat.setStatus(SeatStatus.AVAILABLE);
        seat = seatRepository.save(seat);

        Map<String, Object> createBookingRequest = Map.of(
                "routeId", route.getId(),
                "timeslotId", timeslot.getId(),
                "seatIds", List.of(seat.getId()),
                "passengerName", "Booking User",
                "passengerPhone", "0812345678",
                "pickupLocation", "ABAC Gate",
                "paymentMethod", "PROMPTPAY",
                "sourceChannel", "LIFF",
                "idempotencyKey", "int-payment-proof-create"
        );

        ResponseEntity<Map> createBookingResponse = restTemplate.exchange(
                "/api/liff/bookings",
                HttpMethod.POST,
                authorizedJson(userToken, createBookingRequest),
                Map.class
        );
        assertThat(createBookingResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Map<String, Object> bookingData = (Map<String, Object>) createBookingResponse.getBody().get("data");
        UUID bookingId = UUID.fromString((String) bookingData.get("id"));

        Map<String, Object> submitProofRequest = Map.of(
                "proofImageUrl", "data:image/png;base64,ZmFrZS1wcm9vZg==",
                "proofReference", "KPLUS-123456",
                "paidAt", "2026-04-24T10:15:30Z"
        );

        ResponseEntity<Map> submitProofResponse = restTemplate.exchange(
                "/api/liff/bookings/" + bookingId + "/payment-proof",
                HttpMethod.POST,
                authorizedJson(userToken, submitProofRequest),
                Map.class
        );
        assertThat(submitProofResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Payment payment = paymentRepository.findByBookingId(bookingId).orElseThrow();
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING_REVIEW);
        assertThat(payment.getProofImageUrl()).isEqualTo("data:image/png;base64,ZmFrZS1wcm9vZg==");
        assertThat(payment.getProofReference()).isEqualTo("KPLUS-123456");
        assertThat(payment.getProofSubmittedAt()).isNotNull();
        assertThat(payment.getPaidAt()).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PAYMENT_UNDER_REVIEW);
    }

    @Test
    @SuppressWarnings("unchecked")
    void cancelBookingReleasesSeatAndRestoresTimeslotCapacity() {
        String userToken = registerAndGetToken("cancel.user@auvan.app");

        Route route = new Route();
        route.setFromLocation("ABAC");
        route.setToLocation("BTS Udomsuk");
        route.setSlug("abac-bts-cancel");
        route.setPrice(BigDecimal.valueOf(80));
        route.setDurationMinutes(35);
        route.setStatus(RouteStatus.ACTIVE);
        route = routeRepository.save(route);

        Timeslot timeslot = new Timeslot();
        timeslot.setRoute(route);
        timeslot.setDate(LocalDate.now().plusDays(1));
        timeslot.setTime(LocalTime.of(9, 0));
        timeslot.setTotalSeats(12);
        timeslot.setBookedSeats(0);
        timeslot.setStatus(TimeslotStatus.ACTIVE);
        timeslot = timeslotRepository.save(timeslot);

        Seat seat = new Seat();
        seat.setTimeslot(timeslot);
        seat.setSeatNumber(1);
        seat.setLabel(Seat.buildLabel(1));
        seat.setStatus(SeatStatus.AVAILABLE);
        seat = seatRepository.save(seat);

        Map<String, Object> createBookingRequest = Map.of(
                "routeId", route.getId(),
                "timeslotId", timeslot.getId(),
                "seatIds", List.of(seat.getId()),
                "passengerName", "Booking User",
                "passengerPhone", "0812345678",
                "pickupLocation", "ABAC Gate",
                "paymentMethod", "PROMPTPAY",
                "sourceChannel", "LIFF",
                "idempotencyKey", "int-cancel-create"
        );

        ResponseEntity<Map> createBookingResponse = restTemplate.exchange(
                "/api/liff/bookings",
                HttpMethod.POST,
                authorizedJson(userToken, createBookingRequest),
                Map.class
        );
        assertThat(createBookingResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Map<String, Object> bookingData = (Map<String, Object>) createBookingResponse.getBody().get("data");
        UUID bookingId = UUID.fromString((String) bookingData.get("id"));

        ResponseEntity<Map> cancelResponse = restTemplate.exchange(
                "/api/liff/bookings/" + bookingId,
                HttpMethod.DELETE,
                authorizedJson(userToken, Map.of()),
                Map.class
        );
        assertThat(cancelResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        Seat refreshedSeat = seatRepository.findById(seat.getId()).orElseThrow();
        Timeslot refreshedTimeslot = timeslotRepository.findById(timeslot.getId()).orElseThrow();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(refreshedSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(refreshedSeat.getLockedBy()).isNull();
        assertThat(refreshedTimeslot.getBookedSeats()).isEqualTo(0);
        assertThat(refreshedTimeslot.getStatus()).isEqualTo(TimeslotStatus.ACTIVE);
    }

    @SuppressWarnings("unchecked")
    private String registerAndGetToken(String email) {
        Map<String, Object> registerRequest = Map.of(
                "name", "Booking User",
                "email", email,
                "password", "secret123",
                "phone", "0812345678"
        );
        ResponseEntity<Map> registerResponse = restTemplate.postForEntity(
                "/api/auth/register",
                jsonRequest(registerRequest),
                Map.class
        );

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map<String, Object> data = (Map<String, Object>) registerResponse.getBody().get("data");
        return (String) data.get("token");
    }

    private HttpEntity<Map<String, Object>> jsonRequest(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(payload, headers);
    }

    private HttpEntity<Map<String, Object>> authorizedJson(String token, Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return new HttpEntity<>(payload, headers);
    }
}
