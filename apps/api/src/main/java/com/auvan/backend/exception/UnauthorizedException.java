package com.auvan.backend.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AuVanException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }
}
