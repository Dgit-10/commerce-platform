package com.ecommerce.order_service.kafka;

import com.common_packages.common_packages.event.OrderCreatedEvent;
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
        log.info("Publishing OrderCreatedEvent for Order ID: {}", event.getOrderId());
        kafkaTemplate.send(TOPIC_ORDER_CREATED, String.valueOf(event.getOrderId()), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully dispatched OrderCreatedEvent for Order ID: {} to partition: {}",
                                event.getOrderId(), result.getRecordMetadata().partition());
                    } else {
                        log.error("Failed to dispatch OrderCreatedEvent for Order ID: {}", event.getOrderId(), ex);
                    }
                });
    }
}