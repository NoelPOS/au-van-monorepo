package com.auvan.backend.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record RescheduleRequest(
        @NotNull(message = "New timeslot ID is required")
        UUID timeslotId,

        @NotEmpty(message = "At least one seat must be selected")
        List<UUID> seatIds,

        String idempotencyKey
) {}
