package com.auvan.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record SubmitPaymentProofRequest(
        @NotBlank(message = "Proof image URL is required")
        String proofImageUrl,

        String proofReference,

        Instant paidAt
) {}
