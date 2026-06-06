package com.auvan.backend.timeslot.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record BulkCreateTimeslotRequest(
        @NotNull(message = "Route ID is required")
        UUID routeId,

        @NotNull(message = "Start date is required")
        LocalDate dateFrom,

        @NotNull(message = "End date is required")
        LocalDate dateTo,

        /** ISO day-of-week values: 1=Monday … 7=Sunday */
        @NotEmpty(message = "At least one day of week must be selected")
        List<Integer> daysOfWeek,

        @NotEmpty(message = "At least one departure time must be specified")
        List<LocalTime> times,

        @Min(value = 1,  message = "Must have at least 1 seat")
        @Max(value = 50, message = "Cannot exceed 50 seats")
        int totalSeats
) {}
