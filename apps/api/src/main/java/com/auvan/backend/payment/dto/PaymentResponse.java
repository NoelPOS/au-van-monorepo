package com.auvan.backend.payment.dto;

import com.auvan.backend.payment.PaymentMethod;
import com.auvan.backend.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID          id,
        UUID          bookingId,
        BigDecimal    amount,
        PaymentMethod method,
        PaymentStatus status,
        String        transactionId,
        String        proofImageUrl,
        String        proofReference,
        Instant       proofSubmittedAt,
        UUID          reviewedBy,
        Instant       reviewedAt,
        String        reviewNote,
        Instant       paidAt,
        Instant       refundedAt,
        Instant       createdAt
) {}
