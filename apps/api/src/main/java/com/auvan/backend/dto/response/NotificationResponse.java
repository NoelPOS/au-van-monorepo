package com.auvan.backend.dto.response;

import com.auvan.backend.enums.DeliveryStatus;
import com.auvan.backend.enums.NotificationChannel;
import com.auvan.backend.enums.NotificationType;

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
