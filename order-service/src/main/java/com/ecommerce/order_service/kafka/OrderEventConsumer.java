package com.ecommerce.order_service.kafka;

import com.common_packages.common_packages.event.PaymentProcessedEvent;
import com.ecommerce.order_service.entity.OrderStatus;
import com.ecommerce.order_service.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final OrderService orderService;

    public OrderEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "payment-processed-topic", groupId = "order-service-group")
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("Received PaymentProcessedEvent for Order ID: {}, Status Success: {}",
                event.getOrderId(), event.isSuccessful());

        if (event.isSuccessful()) {
            orderService.updateOrderStatus(event.getOrderId(), OrderStatus.PAID);
        } else {
            log.warn("Payment failed for Order ID: {}. Reason: {}", event.getOrderId(), event.getFailureReason());
            orderService.updateOrderStatus(event.getOrderId(), OrderStatus.CANCELLED);
        }
    }
}