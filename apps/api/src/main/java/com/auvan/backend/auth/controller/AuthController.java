package com.auvan.backend.auth.controller;

import com.auvan.backend.auth.dto.LineAuthRequest;
import com.auvan.backend.auth.dto.LoginRequest;
import com.auvan.backend.auth.dto.RegisterRequest;
import com.auvan.backend.shared.dto.ApiResponse;
import com.auvan.backend.auth.dto.AuthResponse;
import com.auvan.backend.auth.service.AuthService;
import com.auvan.backend.auth.service.LineAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final LineAuthService lineAuthService;

    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registration successful"));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/liff/auth/line")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateWithLine(@Valid @RequestBody LineAuthRequest request) {
        AuthResponse response = lineAuthService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success(response, "LINE authentication successful"));
    }
}
