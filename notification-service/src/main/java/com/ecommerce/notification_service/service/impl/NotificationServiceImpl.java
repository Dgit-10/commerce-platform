package com.ecommerce.notification_service.service.impl;

import com.ecommerce.notification_service.dto.NotificationResponse;
import com.ecommerce.notification_service.entity.Notification;
import com.ecommerce.notification_service.entity.NotificationStatus;
import com.ecommerce.notification_service.entity.NotificationType;
import com.ecommerce.notification_service.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.app.notification.service.NotificationService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void sendNotification(Long userId, NotificationType type, String message, String recipient) {
        log.info("Sending [{}] notification to User ID: {} ({})", type, userId, recipient);

        // Simulate notification dispatch (e.g., via JavaMailSender, Twilio, or Firebase Push)
        boolean isSent = simulateEmailProvider(message, recipient);

        NotificationStatus status = isSent ? NotificationStatus.SENT : NotificationStatus.FAILED;

        Notification notification = new Notification(userId, type, message, recipient, status);
        notificationRepository.save(notification);

        log.info("Notification record saved with ID: {}, Status: {}", notification.getId(), status);
    }

    @Override
    public List<NotificationResponse> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private boolean simulateEmailProvider(String message, String recipient) {
        // Mock external service communication
        return recipient != null && !recipient.isBlank();
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getMessage(),
                notification.getRecipient(),
                notification.getStatus(),
                notification.getCreatedAt()
        );
    }
}
