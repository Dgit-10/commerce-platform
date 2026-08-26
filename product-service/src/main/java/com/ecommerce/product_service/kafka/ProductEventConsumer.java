package com.ecommerce.product_service.kafka;

import com.common_packages.common_packages.event.OrderCreatedEvent;
import com.common_packages.common_packages.tracing.KafkaCorrelationUtils;
import com.ecommerce.product_service.entity.ProcessedEvent;
import com.ecommerce.product_service.repository.ProcessedEventRepository;
import com.ecommerce.product_service.service.ProductService;
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
public class ProductEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer.class);

    private final ProductService productService;
    private final ProcessedEventRepository processedEventRepository;

    public ProductEventConsumer(ProductService productService, ProcessedEventRepository processedEventRepository) {
        this.productService = productService;
        this.processedEventRepository = processedEventRepository;
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "order-created-topic", groupId = "product-service-group")
    @Transactional
    public void handleOrderCreated(ConsumerRecord<String, OrderCreatedEvent> record) {
        KafkaCorrelationUtils.extractCorrelationId(record);
        try {
            OrderCreatedEvent event = record.value();
            String eventKey = "STOCK_DEDUCT_ORDER_" + event.getOrderId();

            if (processedEventRepository.existsByEventKey(eventKey)) {
                log.warn("Idempotency: Stock deduction already performed for Order ID: {}", event.getOrderId());
                return;
            }

            log.info("ProductService processing stock reduction for Order ID: {}", event.getOrderId());

            if (event.getItems() != null) {
                for (OrderCreatedEvent.OrderItemEventDto item : event.getItems()) {
                    try {
                        productService.deductStockForOrder(item.getProductId(), item.getQuantity());
                    } catch (Exception e) {
                        log.error("Failed to deduct stock for Product ID: {} in Order ID: {}. Error: {}",
                                item.getProductId(), event.getOrderId(), e.getMessage());
                    }
                }
            }

            processedEventRepository.save(new ProcessedEvent(eventKey, "STOCK_DEDUCTED"));
        } finally {
            KafkaCorrelationUtils.clear();
        }
    }

    @DltHandler
    public void handleDlt(ConsumerRecord<String, Object> record) {
        log.error("Product Service Dead-Letter-Topic (DLT) received failed record from topic {}: key={}, value={}",
                record.topic(), record.key(), record.value());
    }
}
