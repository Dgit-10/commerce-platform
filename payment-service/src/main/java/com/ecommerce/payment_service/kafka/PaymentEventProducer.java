package com.ecommerce.payment_service.kafka;

import com.common_packages.common_packages.event.PaymentApprovedEvent;
import com.common_packages.common_packages.event.PaymentProcessedEvent;
import com.common_packages.common_packages.tracing.KafkaCorrelationUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);

    public static final String TOPIC_PAYMENT_PROCESSED = "payment-processed-topic";
    public static final String TOPIC_PAYMENT_APPROVED = "payment-approved-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentProcessedEvent(PaymentProcessedEvent event) {
        log.info("Publishing PaymentProcessedEvent for Order ID: {}, Success: {}", event.getOrderId(), event.isSuccessful());

        ProducerRecord<String, Object> record =
                new ProducerRecord<>(TOPIC_PAYMENT_PROCESSED, String.valueOf(event.getOrderId()), event);

        KafkaCorrelationUtils.injectCorrelationId(record);

        kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published PaymentProcessedEvent for Order ID: {}", event.getOrderId());
            } else {
                log.error("Failed to publish PaymentProcessedEvent for Order ID: {}", event.getOrderId(), ex);
            }
        });
    }

    public void publishPaymentApprovedEvent(PaymentApprovedEvent event) {
        log.info("Publishing PaymentApprovedEvent for Payment ID: {}, Order ID: {}", event.getPaymentId(), event.getOrderId());

        ProducerRecord<String, Object> record =
                new ProducerRecord<>(TOPIC_PAYMENT_APPROVED, String.valueOf(event.getOrderId()), event);

        KafkaCorrelationUtils.injectCorrelationId(record);

        kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published PaymentApprovedEvent for Order ID: {} to partition: {}",
                        event.getOrderId(), result.getRecordMetadata().partition());
            } else {
                log.error("Failed to publish PaymentApprovedEvent for Order ID: {}", event.getOrderId(), ex);
            }
        });
    }
}
