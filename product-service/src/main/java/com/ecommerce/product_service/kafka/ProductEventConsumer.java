package com.ecommerce.product_service.kafka;

import com.common_packages.common_packages.event.OrderCreatedEvent;
import com.ecommerce.product_service.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer.class);
    private final ProductService productService;

    public ProductEventConsumer(ProductService productService) {
        this.productService = productService;
    }

    @KafkaListener(topics = "order-created-topic", groupId = "product-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("ProductService processing stock reduction for Order ID: {}", event.getOrderId());

        for (OrderCreatedEvent.OrderItemEventDto item : event.getItems()) {
            try {
                productService.deductStockForOrder(item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to deduct stock for Product ID: {} in Order ID: {}. Error: {}",
                        item.getProductId(), event.getOrderId(), e.getMessage());
                // In an enterprise setup, publish a StockReservationFailedEvent to trigger Saga rollback
            }
        }
    }
}