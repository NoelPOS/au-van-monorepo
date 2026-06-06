package com.auvan.backend.shared.security;

import com.auvan.backend.shared.exception.UnauthorizedException;
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
