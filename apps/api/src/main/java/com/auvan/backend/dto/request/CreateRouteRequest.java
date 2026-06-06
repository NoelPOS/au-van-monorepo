package com.auvan.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateRouteRequest(
        @NotBlank(message = "From location is required")
        String fromLocation,

        @NotBlank(message = "To location is required")
        String toLocation,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        BigDecimal price,

        Integer durationMinutes
) {}
