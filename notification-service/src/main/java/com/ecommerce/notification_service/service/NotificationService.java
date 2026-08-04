package com.ecommerce.app.notification.service;

import com.ecommerce.notification_service.dto.NotificationResponse;
import com.ecommerce.notification_service.entity.NotificationType;

import java.util.List;

public interface NotificationService {
    void sendNotification(Long userId, NotificationType type, String message, String recipient);
    List<NotificationResponse> getNotificationsByUserId(Long userId);
}