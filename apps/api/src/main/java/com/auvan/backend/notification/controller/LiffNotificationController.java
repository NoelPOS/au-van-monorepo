package com.auvan.backend.notification.controller;

import com.auvan.backend.shared.security.CurrentUser;
import com.auvan.backend.shared.dto.ApiResponse;
import com.auvan.backend.notification.dto.NotificationResponse;
import com.auvan.backend.shared.dto.PageResponse;
import com.auvan.backend.shared.security.CustomUserDetails;
import com.auvan.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/liff/notifications")
public class LiffNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<NotificationResponse> response =
                notificationService.getMyNotifications(CurrentUser.id(principal), page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countUnread(
            @AuthenticationPrincipal CustomUserDetails principal) {
        long unread = notificationService.countUnread(CurrentUser.id(principal));
        return ResponseEntity.ok(ApiResponse.success(Map.of("unread", unread)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        NotificationResponse response = notificationService.markRead(id, CurrentUser.id(principal));
        return ResponseEntity.ok(ApiResponse.success(response, "Notification marked as read"));
    }
}
