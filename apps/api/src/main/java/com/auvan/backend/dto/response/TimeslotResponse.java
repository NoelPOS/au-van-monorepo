package com.auvan.backend.dto.response;

import com.auvan.backend.enums.TimeslotStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TimeslotResponse(
        UUID            id,
        UUID            routeId,
        String          routeFromLocation,
        String          routeToLocation,
        LocalDate       date,
        LocalTime       time,
        int             totalSeats,
        int             bookedSeats,
        int             availableSeats,
        TimeslotStatus  status,
        Instant         createdAt,
        Instant         updatedAt
) {}
