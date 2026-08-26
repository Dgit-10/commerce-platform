package com.ecommerce.payment_service.kafka;

import com.common_packages.common_packages.event.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);
    private static final String TOPIC_PAYMENT_PROCESSED = "payment-processed-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentProcessedEvent(PaymentProcessedEvent event) {
        log.info("Publishing PaymentProcessedEvent for Order ID: {}, Status: {}", event.getOrderId(), event.isSuccessful());
        kafkaTemplate.send(TOPIC_PAYMENT_PROCESSED, String.valueOf(event.getOrderId()), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Dispatched PaymentProcessedEvent for Order ID: {}", event.getOrderId());
                    } else {
                        log.error("Failed to dispatch PaymentProcessedEvent for Order ID: {}", event.getOrderId(), ex);
                    }
                });
    }
}