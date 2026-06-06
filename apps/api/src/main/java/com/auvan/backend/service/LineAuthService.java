package com.auvan.backend.service;

import com.auvan.backend.dto.request.LineAuthRequest;
import com.auvan.backend.dto.response.AuthResponse;

public interface LineAuthService {

    /**
     * Verifies the LINE ID token with the LINE platform, upserts the local User,
     * and returns a JWT.
     */
    AuthResponse authenticate(LineAuthRequest request);
}
