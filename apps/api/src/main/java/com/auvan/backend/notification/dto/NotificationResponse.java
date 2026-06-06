package com.auvan.backend.notification.dto;

import com.auvan.backend.notification.enums.DeliveryStatus;
import com.auvan.backend.notification.enums.NotificationChannel;
import com.auvan.backend.notification.enums.NotificationType;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationResponse(
        UUID                 id,
        NotificationType     type,
        String               title,
        String               message,
        boolean              read,
        NotificationChannel  channel,
        DeliveryStatus       deliveryStatus,
        Map<String, Object>  data,
        Instant              createdAt
) {}
