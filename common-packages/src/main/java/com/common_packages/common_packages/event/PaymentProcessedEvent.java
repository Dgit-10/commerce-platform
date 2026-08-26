package com.common_packages.common_packages.event;

public class PaymentProcessedEvent {
    private Long orderId;
    private Long userId;
    private boolean successful;
    private String transactionId;
    private String failureReason;

    public PaymentProcessedEvent() {}

    public PaymentProcessedEvent(Long orderId, Long userId, boolean successful, String transactionId, String failureReason) {
        this.orderId = orderId;
        this.userId = userId;
        this.successful = successful;
        this.transactionId = transactionId;
        this.failureReason = failureReason;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public boolean isSuccessful() { return successful; }
    public void setSuccessful(boolean successful) { this.successful = successful; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}