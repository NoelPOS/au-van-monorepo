package com.auvan.backend.timeslot.dto;

import com.auvan.backend.timeslot.TimeslotStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateTimeslotRequest(
        LocalDate date,
        LocalTime time,

        @Min(value = 1,  message = "Must have at least 1 seat")
        @Max(value = 50, message = "Cannot exceed 50 seats")
        Integer totalSeats,

        TimeslotStatus status
) {}
