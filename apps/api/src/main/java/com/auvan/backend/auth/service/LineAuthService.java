package com.auvan.backend.auth.service;

import com.auvan.backend.auth.dto.LineAuthRequest;
import com.auvan.backend.auth.dto.AuthResponse;

public interface LineAuthService {

    /**
     * Verifies the LINE ID token with the LINE platform, upserts the local User,
     * and returns a JWT.
     */
    AuthResponse authenticate(LineAuthRequest request);
}
