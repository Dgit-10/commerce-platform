package com.ecommerce.notification_service.kafka;

import com.common_packages.common_packages.event.OrderCreatedEvent;
import com.common_packages.common_packages.event.PaymentProcessedEvent;
import com.common_packages.common_packages.event.UserRegisteredEvent;
import com.ecommerce.notification_service.entity.NotificationType;
import com.ecommerce.notification_service.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);
    private final NotificationService notificationService;

    public NotificationEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "user-registered-topic", groupId = "notification-service-group")
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("NotificationService received UserRegisteredEvent for Email: {}", event.getEmail());
        String message = String.format("Welcome to our store, %s! Your account has been created.", event.getFullName());

        notificationService.sendNotification(
                event.getUserId(),
                NotificationType.WELCOME_EMAIL,
                message,
                event.getEmail()
        );
    }

    @KafkaListener(topics = "order-created-topic", groupId = "notification-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("NotificationService received OrderCreatedEvent for Order ID: {}", event.getOrderId());
        String message = String.format("Your order #%d has been received and total amount is $%.2f. Awaiting payment.",
                event.getOrderId(), event.getTotalAmount());

        notificationService.sendNotification(
                event.getUserId(),
                NotificationType.ORDER_CONFIRMATION,
                message,
                "user_" + event.getUserId() + "@ecommerce.com"
        );
    }

    @KafkaListener(topics = "payment-processed-topic", groupId = "notification-service-group")
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("NotificationService received PaymentProcessedEvent for Order ID: {}", event.getOrderId());

        if (event.isSuccessful()) {
            String message = String.format("Payment for Order #%d was successful. Transaction ID: %s",
                    event.getOrderId(), event.getTransactionId());
            notificationService.sendNotification(
                    event.getUserId(),
                    NotificationType.PAYMENT_SUCCESS,
                    message,
                    "user_" + event.getUserId() + "@ecommerce.com"
            );
        } else {
            String message = String.format("Payment failed for Order #%d. Reason: %s",
                    event.getOrderId(), event.getFailureReason());
            notificationService.sendNotification(
                    event.getUserId(),
                    NotificationType.PAYMENT_FAILURE,
                    message,
                    "user_" + event.getUserId() + "@ecommerce.com"
            );
        }
    }
}