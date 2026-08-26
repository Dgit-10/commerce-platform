package com.ecommerce.payment_service.service;

import com.ecommerce.payment_service.dto.PaymentRequest;
import com.ecommerce.payment_service.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);
    PaymentResponse approvePayment(Long paymentId, String approvedBy);
    PaymentResponse approvePaymentByOrderId(Long orderId, String approvedBy);
    PaymentResponse rejectPayment(Long paymentId, String reason);
    PaymentResponse getPaymentById(Long id);
    PaymentResponse getPaymentByOrderId(Long orderId);
    List<PaymentResponse> getPendingPayments();
}
