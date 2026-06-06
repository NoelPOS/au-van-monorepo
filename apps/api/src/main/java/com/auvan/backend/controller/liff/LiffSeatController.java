package com.auvan.backend.controller.liff;

import com.auvan.backend.dto.request.LockSeatsRequest;
import com.auvan.backend.dto.response.ApiResponse;
import com.auvan.backend.dto.response.SeatResponse;
import com.auvan.backend.exception.UnauthorizedException;
import com.auvan.backend.security.CustomUserDetails;
import com.auvan.backend.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/liff/seats")
public class LiffSeatController {

    private final SeatService seatService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SeatResponse>>> getSeatMap(@RequestParam UUID timeslotId) {
        return ResponseEntity.ok(ApiResponse.success(seatService.getSeatMap(timeslotId)));
    }

    @PostMapping("/lock")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> lockSeats(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody LockSeatsRequest request) {
        UUID userId = currentUserId(principal);
        seatService.lockSeats(request.timeslotId(), request.seatIds(), userId);
        List<SeatResponse> updatedSeatMap = seatService.getSeatMap(request.timeslotId());
        return ResponseEntity.ok(ApiResponse.success(updatedSeatMap, "Seats locked"));
    }

    private UUID currentUserId(CustomUserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return principal.getUserId();
    }
}
