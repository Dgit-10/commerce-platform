package com.ecommerce.order_service.kafka;

import com.common_packages.common_packages.event.OrderCreatedEvent;
import com.common_packages.common_packages.tracing.KafkaCorrelationUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    private static final String TOPIC_ORDER_CREATED = "order-created-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent for Order ID: {}, Amount: {}", event.getOrderId(), event.getTotalAmount());

        ProducerRecord<String, Object> record =
                new ProducerRecord<>(TOPIC_ORDER_CREATED, String.valueOf(event.getOrderId()), event);

        KafkaCorrelationUtils.injectCorrelationId(record);

        kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully published OrderCreatedEvent for Order ID: {} to partition: {}",
                                event.getOrderId(), result.getRecordMetadata().partition());
                    } else {
                        log.error("Failed to publish OrderCreatedEvent for Order ID: {}", event.getOrderId(), ex);
                    }
                });
    }
}
