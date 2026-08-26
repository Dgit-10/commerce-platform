package com.ecommerce.payment_service.kafka;

import com.common_packages.common_packages.event.OrderCreatedEvent;
import com.ecommerce.payment_service.dto.PaymentRequest;
import com.ecommerce.payment_service.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);
    private final PaymentService paymentService;

    public PaymentEventConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "order-created-topic", groupId = "payment-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for Order ID: {}, Amount: {}", event.getOrderId(), event.getTotalAmount());

        PaymentRequest request = new PaymentRequest(
                event.getOrderId(),
                event.getUserId(),
                event.getTotalAmount()
        );

        paymentService.processPayment(request);
    }
}