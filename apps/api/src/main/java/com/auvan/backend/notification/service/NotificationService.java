package com.auvan.backend.notification.service;

import com.auvan.backend.notification.dto.NotificationResponse;
import com.auvan.backend.shared.dto.PageResponse;
import com.auvan.backend.user.User;
import com.auvan.backend.notification.NotificationType;

import java.util.Map;
import java.util.UUID;

public interface NotificationService {

    PageResponse<NotificationResponse> getMyNotifications(UUID userId, int page, int size);

    NotificationResponse markRead(UUID notificationId, UUID userId);

    long countUnread(UUID userId);

    void sendInApp(User user, NotificationType type, String title, String message, Map<String, Object> data);
}
