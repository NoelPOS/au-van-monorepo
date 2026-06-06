package com.auvan.backend.exception;

import org.springframework.http.HttpStatus;

public class PaymentDeadlineException extends AuVanException {

    public PaymentDeadlineException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "PAYMENT_DEADLINE_PASSED");
    }
}
