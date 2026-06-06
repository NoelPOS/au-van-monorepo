package com.auvan.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LineAuthRequest(
        @NotBlank(message = "LINE ID token is required")
        String idToken,

        String phone
) {}
