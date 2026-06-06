package com.auvan.backend.seat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record LockSeatsRequest(
        @NotNull(message = "Timeslot ID is required")
        UUID timeslotId,

        @NotEmpty(message = "At least one seat must be specified")
        List<UUID> seatIds
) {}
