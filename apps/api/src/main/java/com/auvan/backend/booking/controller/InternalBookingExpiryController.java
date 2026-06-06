package com.auvan.backend.booking.controller;

import com.auvan.backend.shared.dto.ApiResponse;
import com.auvan.backend.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/bookings")
public class InternalBookingExpiryController {

    private final BookingService bookingService;

    @PostMapping("/expire-unpaid")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> expireUnpaid(
            @RequestParam(defaultValue = "30") int limit) {
        int expired = bookingService.expireUnpaidBookings(limit);
        return ResponseEntity.ok(ApiResponse.success(Map.of("expired", expired)));
    }
}
