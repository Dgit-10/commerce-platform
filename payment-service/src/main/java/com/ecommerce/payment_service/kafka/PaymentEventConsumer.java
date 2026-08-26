package com.ecommerce.payment_service.kafka;

import com.common_packages.common_packages.event.OrderCreatedEvent;
import com.common_packages.common_packages.tracing.KafkaCorrelationUtils;
import com.ecommerce.payment_service.dto.PaymentRequest;
import com.ecommerce.payment_service.entity.ProcessedEvent;
import com.ecommerce.payment_service.repository.ProcessedEventRepository;
import com.ecommerce.payment_service.service.PaymentService;
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
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final PaymentService paymentService;
    private final ProcessedEventRepository processedEventRepository;

    public PaymentEventConsumer(PaymentService paymentService, ProcessedEventRepository processedEventRepository) {
        this.paymentService = paymentService;
        this.processedEventRepository = processedEventRepository;
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "order-created-topic", groupId = "payment-service-group")
    @Transactional
    public void handleOrderCreated(ConsumerRecord<String, OrderCreatedEvent> record) {
        KafkaCorrelationUtils.extractCorrelationId(record);
        try {
            OrderCreatedEvent event = record.value();
            String eventKey = "ORDER_CREATED_" + event.getOrderId();

            if (processedEventRepository.existsByEventKey(eventKey)) {
                log.warn("Idempotency: Payment Service already processed Order ID: {}", event.getOrderId());
                return;
            }

            log.info("Received OrderCreatedEvent for Order ID: {}, Amount: {}", event.getOrderId(), event.getTotalAmount());

            PaymentRequest request = new PaymentRequest(
                    event.getOrderId(),
                    event.getUserId(),
                    event.getTotalAmount()
            );

            paymentService.processPayment(request);
            processedEventRepository.save(new ProcessedEvent(eventKey, "ORDER_CREATED"));
        } finally {
            KafkaCorrelationUtils.clear();
        }
    }

    @DltHandler
    public void handleDlt(ConsumerRecord<String, Object> record) {
        log.error("Payment Service Dead-Letter-Topic (DLT) received failed record from topic {}: key={}, value={}",
                record.topic(), record.key(), record.value());
    }
}
