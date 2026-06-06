package com.auvan.backend.reminder.dto;

import com.auvan.backend.reminder.enums.ReminderStatus;
import com.auvan.backend.reminder.enums.ReminderType;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ReminderJobResponse(
        UUID id,
        UUID bookingId,
        UUID userId,
        ReminderType type,
        Instant runAt,
        ReminderStatus status,
        int attempts,
        Instant lockedAt,
        Instant sentAt,
        String lastError,
        Map<String, Object> payload,
        Instant createdAt
) {}
