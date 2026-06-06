package com.auvan.backend.dto.request;

import com.auvan.backend.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record ReviewPaymentRequest(
        @NotNull(message = "Payment status is required")
        PaymentStatus status,

        String transactionId,
        String reviewNote
) {}
