package com.auvan.backend.seat.dto;

import com.auvan.backend.seat.enums.SeatStatus;
import java.util.UUID;

public record SeatResponse(
        UUID       id,
        int        seatNumber,
        String     label,
        SeatStatus status
) {}
