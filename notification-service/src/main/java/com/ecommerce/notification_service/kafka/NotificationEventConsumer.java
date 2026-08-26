package com.ecommerce.notification_service.kafka;

import com.common_packages.common_packages.event.OrderCreatedEvent;
import com.common_packages.common_packages.event.PaymentApprovedEvent;
import com.common_packages.common_packages.event.PaymentProcessedEvent;
import com.common_packages.common_packages.event.UserRegisteredEvent;
import com.common_packages.common_packages.tracing.KafkaCorrelationUtils;
import com.ecommerce.notification_service.entity.NotificationType;
import com.ecommerce.notification_service.entity.ProcessedEvent;
import com.ecommerce.notification_service.repository.ProcessedEventRepository;
import com.ecommerce.notification_service.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;
    private final ProcessedEventRepository processedEventRepository;

    public NotificationEventConsumer(NotificationService notificationService,
                                     ProcessedEventRepository processedEventRepository) {
        this.notificationService = notificationService;
        this.processedEventRepository = processedEventRepository;
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "user-registered-topic", groupId = "notification-service-group")
    @Transactional
    public void handleUserRegistered(ConsumerRecord<String, UserRegisteredEvent> record) {
        KafkaCorrelationUtils.extractCorrelationId(record);
        try {
            UserRegisteredEvent event = record.value();
            String eventKey = "USER_REGISTERED_" + event.getUserId();

            if (processedEventRepository.existsByEventKey(eventKey)) {
                log.warn("Idempotency: Notification Service already sent welcome email for User ID: {}", event.getUserId());
                return;
            }

            log.info("NotificationService received UserRegisteredEvent for Email: {}", event.getEmail());
            String message = String.format("Welcome to our store, %s! Your account has been created successfully.", event.getFullName());

            notificationService.sendNotification(
                    event.getUserId(),
                    NotificationType.WELCOME_EMAIL,
                    message,
                    event.getEmail()
            );

            processedEventRepository.save(new ProcessedEvent(eventKey, "USER_REGISTERED"));
        } finally {
            KafkaCorrelationUtils.clear();
        }
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "payment-approved-topic", groupId = "notification-service-group")
    @Transactional
    public void handlePaymentApproved(ConsumerRecord<String, PaymentApprovedEvent> record) {
        KafkaCorrelationUtils.extractCorrelationId(record);
        try {
            PaymentApprovedEvent event = record.value();
            String eventKey = "PAYMENT_APPROVED_NOTIFICATION_" + event.getOrderId() + "_" + event.getTransactionId();

            if (processedEventRepository.existsByEventKey(eventKey)) {
                log.warn("Idempotency: Order Confirmation notification already sent for Order ID: {}", event.getOrderId());
                return;
            }

            log.info("NotificationService received verified PaymentApprovedEvent for Order ID: {}, Txn: {}. Dispatching Order & Payment Confirmation...",
                    event.getOrderId(), event.getTransactionId());

            String message = String.format(
                    "Order Confirmation: Payment of $%.2f for Order #%d has been verified & approved by %s! Transaction ID: %s. Your order is now being processed for shipment.",
                    event.getAmount(), event.getOrderId(), event.getApprovedBy(), event.getTransactionId()
            );

            notificationService.sendNotification(
                    event.getUserId(),
                    NotificationType.ORDER_CONFIRMATION,
                    message,
                    "user_" + event.getUserId() + "@ecommerce.com"
            );

            processedEventRepository.save(new ProcessedEvent(eventKey, "PAYMENT_APPROVED"));
        } finally {
            KafkaCorrelationUtils.clear();
        }
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "payment-processed-topic", groupId = "notification-service-group")
    @Transactional
    public void handlePaymentProcessed(ConsumerRecord<String, PaymentProcessedEvent> record) {
        KafkaCorrelationUtils.extractCorrelationId(record);
        try {
            PaymentProcessedEvent event = record.value();

            // Only process payment failure events from this topic (approvals are handled above via payment-approved-topic)
            if (!event.isSuccessful()) {
                String eventKey = "PAYMENT_FAILURE_NOTIFICATION_" + event.getOrderId();

                if (processedEventRepository.existsByEventKey(eventKey)) {
                    log.warn("Idempotency: Payment Failure notification already sent for Order ID: {}", event.getOrderId());
                    return;
                }

                log.info("NotificationService received Payment Failure for Order ID: {}", event.getOrderId());
                String message = String.format("Payment failed for Order #%d. Reason: %s. Please update your payment method.",
                        event.getOrderId(), event.getFailureReason());

                notificationService.sendNotification(
                        event.getUserId(),
                        NotificationType.PAYMENT_FAILURE,
                        message,
                        "user_" + event.getUserId() + "@ecommerce.com"
                );

                processedEventRepository.save(new ProcessedEvent(eventKey, "PAYMENT_FAILURE"));
            }
        } finally {
            KafkaCorrelationUtils.clear();
        }
    }

    @DltHandler
    public void handleDlt(ConsumerRecord<String, Object> record) {
        log.error("Notification Service Dead-Letter-Topic (DLT) received failed record from topic {}: key={}, value={}",
                record.topic(), record.key(), record.value());
    }
}
