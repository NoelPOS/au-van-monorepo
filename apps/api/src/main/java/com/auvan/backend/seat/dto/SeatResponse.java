package com.auvan.backend.seat.dto;

import com.auvan.backend.seat.SeatStatus;

import java.util.UUID;

public record SeatResponse(
        UUID       id,
        int        seatNumber,
        String     label,
        SeatStatus status
) {}
