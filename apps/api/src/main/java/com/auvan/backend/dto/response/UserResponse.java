package com.auvan.backend.dto.response;

import com.auvan.backend.enums.AuthProvider;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID         id,
        String       email,
        String       lineUserId,
        AuthProvider authProvider,
        String       displayName,
        String       name,
        String       phone,
        String       defaultPickupLocation,
        String       profileImageUrl,
        boolean      isAdmin,
        Instant      createdAt
) {}
