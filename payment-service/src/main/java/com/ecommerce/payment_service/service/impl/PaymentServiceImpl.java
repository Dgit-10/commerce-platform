package com.ecommerce.payment_service.service.impl;

import com.common_packages.common_packages.event.PaymentProcessedEvent;
import com.common_packages.common_packages.exception.ResourceNotFoundException;
import com.ecommerce.payment_service.dto.PaymentRequest;
import com.ecommerce.payment_service.dto.PaymentResponse;
import com.ecommerce.payment_service.entity.Payment;
import com.ecommerce.payment_service.entity.PaymentStatus;
import com.ecommerce.payment_service.kafka.PaymentEventProducer;
import com.ecommerce.payment_service.repository.PaymentRepository;
import com.ecommerce.payment_service.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // Simulate payment Gateway verification logic
        // In real scenario, integrate Stripe/PayPal API client here.
        boolean isSuccess = request.getAmount().doubleValue() < 10000.0; // Fail orders over $10k as simulation rule

        PaymentStatus status = isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        String txnId = isSuccess ? "TXN_" + UUID.randomUUID().toString().substring(0, 8) : null;
        String failureReason = isSuccess ? null : "Transaction amount exceeds threshold or credit card declined";

        Payment payment = new Payment(
                request.getOrderId(),
                request.getUserId(),
                request.getAmount(),
                status,
                txnId,
                failureReason
        );

        Payment savedPayment = paymentRepository.save(payment);

        // Emit Kafka Event for Order Service & Notification Service
        PaymentProcessedEvent event = new PaymentProcessedEvent(
                savedPayment.getOrderId(),
                savedPayment.getUserId(),
                isSuccess,
                txnId,
                failureReason
        );
        paymentEventProducer.publishPaymentProcessedEvent(event);

        return mapToResponse(savedPayment);
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for Order ID: " + orderId));
        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getTransactionId(),
                payment.getFailureReason(),
                payment.getCreatedAt()
        );
    }
}