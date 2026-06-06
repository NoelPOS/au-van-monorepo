package com.auvan.backend.service;

import com.auvan.backend.dto.request.LoginRequest;
import com.auvan.backend.dto.request.RegisterRequest;
import com.auvan.backend.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
