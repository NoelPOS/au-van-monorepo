package com.auvan.backend.controller.admin;

import com.auvan.backend.dto.response.ApiResponse;
import com.auvan.backend.dto.response.BookingResponse;
import com.auvan.backend.dto.response.PageResponse;
import com.auvan.backend.exception.UnauthorizedException;
import com.auvan.backend.security.CustomUserDetails;
import com.auvan.backend.service.BookingService;
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
        BookingResponse response = bookingService.getById(id, currentUserId(principal));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        bookingService.cancel(id, currentUserId(principal), true);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled"));
    }

    private UUID currentUserId(CustomUserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return principal.getUserId();
    }
}
