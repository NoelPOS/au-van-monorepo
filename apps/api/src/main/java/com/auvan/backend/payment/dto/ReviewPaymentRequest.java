package com.auvan.backend.payment.dto;

import com.auvan.backend.payment.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record ReviewPaymentRequest(
        @NotNull(message = "Payment status is required")
        PaymentStatus status,

        String transactionId,
        String reviewNote
) {}
