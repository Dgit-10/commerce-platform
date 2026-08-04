package com.ecommerce.notification_service.dto;

import com.ecommerce.notification_service.entity.NotificationStatus;
import com.ecommerce.notification_service.entity.NotificationType;

import java.time.LocalDateTime;

public class NotificationResponse {
    private Long id;
    private Long userId;
    private NotificationType type;
    private String message;
    private String recipient;
    private NotificationStatus status;
    private LocalDateTime createdAt;

    public NotificationResponse(Long id, Long userId, NotificationType type, String message,
                                String recipient, NotificationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.recipient = recipient;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public NotificationType getType() { return type; }
    public String getMessage() { return message; }
    public String getRecipient() { return recipient; }
    public NotificationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
