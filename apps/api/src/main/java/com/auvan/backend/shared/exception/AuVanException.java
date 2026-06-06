package com.auvan.backend.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for all AU Van application errors.
 * Subclasses define the appropriate HTTP status and a machine-readable error code.
 */
public class AuVanException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String     errorCode;

    public AuVanException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode  = errorCode;
    }

    public AuVanException(String message, HttpStatus httpStatus) {
        this(message, httpStatus, httpStatus.name());
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String     getErrorCode()  { return errorCode; }
}
