package com.auvan.backend.audit.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
        UUID                id,
        UUID                actorId,
        String              action,
        String              targetType,
        String              targetId,
        Map<String, Object> metadata,
        String              ip,
        Instant             createdAt
) {}
