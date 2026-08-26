package com.ecommerce.payment_service.entity;

public enum PaymentStatus {
    PENDING,
    AWAITING_APPROVAL,
    APPROVED,
    REJECTED,
    FAILED,
    REFUNDED
}
