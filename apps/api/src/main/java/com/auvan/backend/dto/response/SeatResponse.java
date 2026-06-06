package com.auvan.backend.dto.response;

import com.auvan.backend.enums.SeatStatus;

import java.util.UUID;

public record SeatResponse(
        UUID       id,
        int        seatNumber,
        String     label,
        SeatStatus status
) {}
