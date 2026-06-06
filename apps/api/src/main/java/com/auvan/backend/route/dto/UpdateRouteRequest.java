package com.auvan.backend.route.dto;

import com.auvan.backend.route.enums.RouteStatus;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record UpdateRouteRequest(
        String fromLocation,
        String toLocation,

        @Positive(message = "Price must be positive")
        BigDecimal price,

        Integer durationMinutes,
        RouteStatus status
) {}
