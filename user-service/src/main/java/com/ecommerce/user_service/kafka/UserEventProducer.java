package com.ecommerce.user_service.kafka;

import com.common_packages.common_packages.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventProducer {

    private static final Logger log = LoggerFactory.getLogger(UserEventProducer.class);
    private static final String TOPIC_USER_EVENTS = "user-registered-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Publishing UserRegisteredEvent for User ID: {}", event.getUserId());
        kafkaTemplate.send(TOPIC_USER_EVENTS, String.valueOf(event.getUserId()), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully published event for User ID: {} to partition: {}",
                                event.getUserId(), result.getRecordMetadata().partition());
                    } else {
                        log.error("Failed to publish event for User ID: {}", event.getUserId(), ex);
                    }
                });
    }
}