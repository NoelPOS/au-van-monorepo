package com.auvan.backend.controller.liff;

import com.auvan.backend.dto.response.ApiResponse;
import com.auvan.backend.dto.response.TimeslotResponse;
import com.auvan.backend.service.TimeslotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/liff/timeslots")
public class LiffTimeslotController {

    private final TimeslotService timeslotService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeslotResponse>>> listAvailable(
            @RequestParam UUID routeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate) {
        List<TimeslotResponse> response = timeslotService.listAvailable(routeId, fromDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
