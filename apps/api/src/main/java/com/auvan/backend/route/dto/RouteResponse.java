package com.auvan.backend.route.dto;

import com.auvan.backend.route.enums.RouteStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RouteResponse(
        UUID        id,
        String      fromLocation,
        String      toLocation,
        String      slug,
        BigDecimal  price,
        Integer     durationMinutes,
        RouteStatus status,
        Instant     createdAt,
        Instant     updatedAt
) {}
