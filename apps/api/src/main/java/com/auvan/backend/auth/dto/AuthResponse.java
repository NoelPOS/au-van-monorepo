package com.auvan.backend.auth.dto;

import com.auvan.backend.user.dto.UserResponse;

public record AuthResponse(
        String       token,
        String       tokenType,
        long         expiresIn,
        UserResponse user
) {
    public AuthResponse(String token, UserResponse user) {
        this(token, "Bearer", 86400, user);
    }
}
