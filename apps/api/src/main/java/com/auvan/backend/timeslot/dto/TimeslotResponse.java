package com.auvan.backend.timeslot.dto;

import com.auvan.backend.timeslot.enums.TimeslotStatus;
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
