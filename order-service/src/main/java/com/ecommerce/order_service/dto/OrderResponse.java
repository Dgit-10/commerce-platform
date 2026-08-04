package com.ecommerce.order_service.dto;

import com.ecommerce.order_service.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    private Long orderId;
    private Long userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;

    public OrderResponse(Long orderId, Long userId, OrderStatus status, BigDecimal totalAmount, List<OrderItemDto> items, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.items = items;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public Long getOrderId() { return orderId; }
    public Long getUserId() { return userId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<OrderItemDto> getItems() { return items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}