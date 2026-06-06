package com.auvan.backend.booking.controller;

import com.auvan.backend.shared.security.CurrentUser;
import com.auvan.backend.booking.dto.CreateBookingRequest;
import com.auvan.backend.booking.dto.RescheduleRequest;
import com.auvan.backend.payment.dto.SubmitPaymentProofRequest;
import com.auvan.backend.booking.dto.UpdateBookingRequest;
import com.auvan.backend.shared.dto.ApiResponse;
import com.auvan.backend.booking.dto.BookingResponse;
import com.auvan.backend.payment.dto.PaymentResponse;
import com.auvan.backend.shared.security.CustomUserDetails;
import com.auvan.backend.booking.service.BookingService;
import com.auvan.backend.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/liff/bookings")
public class LiffBookingController {

    private final BookingService bookingService;
    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal CustomUserDetails principal) {
        List<BookingResponse> bookings = bookingService.getMyBookings(CurrentUser.id(principal));
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(CurrentUser.id(principal), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Booking created"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getById(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        BookingResponse response = bookingService.getById(id, CurrentUser.id(principal));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> update(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBookingRequest request) {
        BookingResponse response = bookingService.update(id, CurrentUser.id(principal), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Booking updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        bookingService.cancel(id, CurrentUser.id(principal), false);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled"));
    }

    @PostMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<BookingResponse>> reschedule(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleRequest request) {
        BookingResponse response = bookingService.reschedule(id, CurrentUser.id(principal), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Booking rescheduled"));
    }

    @PostMapping("/{id}/payment-proof")
    public ResponseEntity<ApiResponse<PaymentResponse>> submitPaymentProof(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody SubmitPaymentProofRequest request) {
        PaymentResponse response = paymentService.submitProof(id, CurrentUser.id(principal), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment proof submitted"));
    }
}
