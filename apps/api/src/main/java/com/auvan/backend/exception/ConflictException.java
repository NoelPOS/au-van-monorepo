package com.auvan.backend.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends AuVanException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
    }

    public ConflictException(String message, String errorCode) {
        super(message, HttpStatus.CONFLICT, errorCode);
    }
}
