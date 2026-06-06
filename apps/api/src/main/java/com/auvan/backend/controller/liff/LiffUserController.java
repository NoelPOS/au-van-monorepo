package com.auvan.backend.controller.liff;

import com.auvan.backend.dto.request.UpdateUserRequest;
import com.auvan.backend.dto.response.ApiResponse;
import com.auvan.backend.dto.response.UserResponse;
import com.auvan.backend.exception.UnauthorizedException;
import com.auvan.backend.security.CustomUserDetails;
import com.auvan.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/liff/users")
public class LiffUserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getById(currentUserId(principal))));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateProfile(currentUserId(principal), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated"));
    }

    private UUID currentUserId(CustomUserDetails principal) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return principal.getUserId();
    }
}
