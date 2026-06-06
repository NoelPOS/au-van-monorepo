package com.auvan.backend.service.impl;

import com.auvan.backend.mapper.EntityMappers;

import com.auvan.backend.dto.response.NotificationResponse;
import com.auvan.backend.dto.response.PageResponse;
import com.auvan.backend.entity.Notification;
import com.auvan.backend.entity.User;
import com.auvan.backend.enums.DeliveryStatus;
import com.auvan.backend.enums.NotificationChannel;
import com.auvan.backend.enums.NotificationType;
import com.auvan.backend.exception.ForbiddenException;
import com.auvan.backend.exception.ResourceNotFoundException;

import com.auvan.backend.repository.NotificationRepository;
import com.auvan.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EntityMappers mappers;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(UUID userId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                        .map(mappers::toNotification));
    }

    @Override
    @Transactional
    public NotificationResponse markRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this notification");
        }

        notification.setRead(true);
        return mappers.toNotification(notificationRepository.save(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void sendInApp(User user, NotificationType type,
                          String title, String message, Map<String, Object> data) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setChannel(NotificationChannel.INAPP);
        notification.setDeliveryStatus(DeliveryStatus.SENT);
        notification.setData(data);
        notificationRepository.save(notification);
    }
}
