package com.auvan.backend.timeslot.controller;

import com.auvan.backend.timeslot.dto.BulkCreateTimeslotRequest;
import com.auvan.backend.timeslot.dto.CreateTimeslotRequest;
import com.auvan.backend.timeslot.dto.UpdateTimeslotRequest;
import com.auvan.backend.shared.dto.ApiResponse;
import com.auvan.backend.timeslot.dto.TimeslotResponse;
import com.auvan.backend.timeslot.service.TimeslotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@RequestMapping("/api/admin/timeslots")
public class AdminTimeslotController {

    private final TimeslotService timeslotService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeslotResponse>>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(timeslotService.listAll(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TimeslotResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(timeslotService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TimeslotResponse>> create(@Valid @RequestBody CreateTimeslotRequest request) {
        TimeslotResponse response = timeslotService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Timeslot created"));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<TimeslotResponse>>> bulkCreate(
            @Valid @RequestBody BulkCreateTimeslotRequest request) {
        List<TimeslotResponse> response = timeslotService.bulkCreate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Timeslots created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TimeslotResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTimeslotRequest request) {
        TimeslotResponse response = timeslotService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Timeslot updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        timeslotService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Timeslot deleted"));
    }
}
