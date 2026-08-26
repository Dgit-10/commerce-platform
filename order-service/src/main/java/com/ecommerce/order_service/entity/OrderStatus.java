package com.ecommerce.order_service.entity;

public enum OrderStatus {
    PENDING,
    AWAITING_PAYMENT_APPROVAL,
    PAID,
    CANCELLED,
    SHIPPED,
    DELIVERED
}
