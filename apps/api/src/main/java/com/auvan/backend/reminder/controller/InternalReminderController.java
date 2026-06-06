package com.auvan.backend.reminder.controller;

import com.auvan.backend.shared.dto.ApiResponse;
import com.auvan.backend.reminder.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/reminders")
public class InternalReminderController {

    private final ReminderService reminderService;

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> run(@RequestParam(defaultValue = "50") int limit) {
        int processed = reminderService.processPendingReminders(limit);
        return ResponseEntity.ok(ApiResponse.success(Map.of("processed", processed)));
    }
}
