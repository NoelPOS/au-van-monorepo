package com.auvan.backend.controller;

import com.auvan.backend.exception.UnauthorizedException;
import com.auvan.backend.security.CustomUserDetails;

import java.util.UUID;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static UUID id(CustomUserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return principal.getUserId();
    }
}
