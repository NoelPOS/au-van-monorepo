package com.auvan.backend.controller;

import com.auvan.backend.controller.auth.AuthController;
import com.auvan.backend.dto.request.LoginRequest;
import com.auvan.backend.dto.request.RegisterRequest;
import com.auvan.backend.dto.response.ApiResponse;
import com.auvan.backend.dto.response.AuthResponse;
import com.auvan.backend.dto.response.UserResponse;
import com.auvan.backend.enums.AuthProvider;
import com.auvan.backend.service.AuthService;
import com.auvan.backend.service.LineAuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;
    @Mock private LineAuthService lineAuthService;

    @InjectMocks private AuthController authController;

    @Test
    void registerReturns201AndEnvelope() {
        AuthResponse authResponse = sampleAuthResponse();
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        var response = authController.register(new RegisterRequest("Alice", "alice@auvan.app", "secret123", "08123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().token()).isEqualTo("jwt-token");
    }

    @Test
    void loginReturns200AndEnvelope() {
        when(authService.login(any(LoginRequest.class))).thenReturn(sampleAuthResponse());

        var response = authController.login(new LoginRequest("alice@auvan.app", "secret123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<AuthResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isTrue();
        assertThat(body.data().tokenType()).isEqualTo("Bearer");
    }

    private AuthResponse sampleAuthResponse() {
        return new AuthResponse(
                "jwt-token",
                "Bearer",
                86400,
                new UserResponse(
                        UUID.randomUUID(),
                        "alice@auvan.app",
                        null,
                        AuthProvider.LOCAL,
                        null,
                        "Alice",
                        "08123",
                        null,
                        null,
                        false,
                        Instant.now()
                )
        );
    }
}
