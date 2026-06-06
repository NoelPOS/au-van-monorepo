package com.auvan.backend.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends AuVanException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }
}
