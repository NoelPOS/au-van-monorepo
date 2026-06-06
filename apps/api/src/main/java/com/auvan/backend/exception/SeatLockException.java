package com.auvan.backend.exception;

import org.springframework.http.HttpStatus;

public class SeatLockException extends AuVanException {

    public SeatLockException(String message) {
        super(message, HttpStatus.CONFLICT, "SEAT_NOT_AVAILABLE");
    }
}
