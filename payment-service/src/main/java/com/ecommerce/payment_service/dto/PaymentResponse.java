package com.ecommerce.payment_service.dto;

import com.ecommerce.payment_service.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String transactionId;
    private String failureReason;
    private LocalDateTime createdAt;

    public PaymentResponse(Long paymentId, Long orderId, Long userId, BigDecimal amount,
                           PaymentStatus status, String transactionId, String failureReason, LocalDateTime createdAt) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.transactionId = transactionId;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getPaymentId() { return paymentId; }
    public Long getOrderId() { return orderId; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getTransactionId() { return transactionId; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
