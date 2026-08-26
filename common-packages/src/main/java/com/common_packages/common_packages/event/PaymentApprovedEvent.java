package com.common_packages.common_packages.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentApprovedEvent {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String transactionId;
    private String approvedBy;
    private LocalDateTime approvedAt;

    public PaymentApprovedEvent() {}

    public PaymentApprovedEvent(Long paymentId, Long orderId, Long userId, BigDecimal amount, String transactionId, String approvedBy, LocalDateTime approvedAt) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.transactionId = transactionId;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
    }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
}
