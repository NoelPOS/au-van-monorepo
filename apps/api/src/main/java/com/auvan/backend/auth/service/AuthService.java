package com.auvan.backend.auth.service;

import com.auvan.backend.auth.dto.LoginRequest;
import com.auvan.backend.auth.dto.RegisterRequest;
import com.auvan.backend.auth.dto.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
