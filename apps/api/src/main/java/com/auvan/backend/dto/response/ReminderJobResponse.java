package com.auvan.backend.dto.response;

import com.auvan.backend.enums.ReminderStatus;
import com.auvan.backend.enums.ReminderType;

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
