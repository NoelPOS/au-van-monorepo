package com.auvan.backend.booking.controller;

import com.auvan.backend.shared.security.CurrentUser;
import com.auvan.backend.shared.dto.ApiResponse;
import com.auvan.backend.booking.dto.BookingResponse;
import com.auvan.backend.shared.dto.PageResponse;
import com.auvan.backend.shared.security.CustomUserDetails;
import com.auvan.backend.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@RequestMapping("/api/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.listAll(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getById(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        BookingResponse response = bookingService.getById(id, CurrentUser.id(principal));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        bookingService.cancel(id, CurrentUser.id(principal), true);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled"));
    }
}
