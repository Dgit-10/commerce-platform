package com.ecommerce.order_service.kafka;

import com.common_packages.common_packages.event.PaymentApprovedEvent;
import com.common_packages.common_packages.event.PaymentProcessedEvent;
import com.common_packages.common_packages.tracing.KafkaCorrelationUtils;
import com.ecommerce.order_service.entity.OrderStatus;
import com.ecommerce.order_service.entity.ProcessedEvent;
import com.ecommerce.order_service.repository.ProcessedEventRepository;
import com.ecommerce.order_service.service.OrderService;
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
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final OrderService orderService;
    private final ProcessedEventRepository processedEventRepository;

    public OrderEventConsumer(OrderService orderService, ProcessedEventRepository processedEventRepository) {
        this.orderService = orderService;
        this.processedEventRepository = processedEventRepository;
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "payment-approved-topic", groupId = "order-service-group")
    @Transactional
    public void handlePaymentApproved(ConsumerRecord<String, PaymentApprovedEvent> record) {
        KafkaCorrelationUtils.extractCorrelationId(record);
        try {
            PaymentApprovedEvent event = record.value();
            String eventKey = "PAYMENT_APPROVED_" + event.getOrderId() + "_" + event.getTransactionId();

            if (processedEventRepository.existsByEventKey(eventKey)) {
                log.warn("Idempotency: Order Service already processed event key: {}", eventKey);
                return;
            }

            log.info("Received PaymentApprovedEvent for Order ID: {}, Txn: {}", event.getOrderId(), event.getTransactionId());

            orderService.updateOrderStatus(event.getOrderId(), OrderStatus.PAID);
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
    @KafkaListener(topics = "payment-processed-topic", groupId = "order-service-group")
    @Transactional
    public void handlePaymentProcessed(ConsumerRecord<String, PaymentProcessedEvent> record) {
        KafkaCorrelationUtils.extractCorrelationId(record);
        try {
            PaymentProcessedEvent event = record.value();
            String eventKey = "PAYMENT_PROCESSED_" + event.getOrderId() + "_" + event.isSuccessful();

            if (processedEventRepository.existsByEventKey(eventKey)) {
                log.warn("Idempotency: Order Service already processed event key: {}", eventKey);
                return;
            }

            log.info("Received PaymentProcessedEvent for Order ID: {}, Success: {}", event.getOrderId(), event.isSuccessful());

            if (!event.isSuccessful()) {
                log.warn("Payment failed for Order ID: {}. Reason: {}", event.getOrderId(), event.getFailureReason());
                orderService.updateOrderStatus(event.getOrderId(), OrderStatus.CANCELLED);
            }
            processedEventRepository.save(new ProcessedEvent(eventKey, "PAYMENT_PROCESSED"));
        } finally {
            KafkaCorrelationUtils.clear();
        }
    }

    @DltHandler
    public void handleDlt(ConsumerRecord<String, Object> record) {
        log.error("Order Service Dead-Letter-Topic (DLT) received failed record from topic {}: key={}, value={}",
                record.topic(), record.key(), record.value());
    }
}
