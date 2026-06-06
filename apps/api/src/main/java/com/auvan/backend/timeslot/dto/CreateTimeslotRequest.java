package com.auvan.backend.timeslot.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateTimeslotRequest(
        @NotNull(message = "Route ID is required")
        UUID routeId,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Time is required")
        LocalTime time,

        @Min(value = 1,  message = "Must have at least 1 seat")
        @Max(value = 50, message = "Cannot exceed 50 seats")
        int totalSeats
) {}
