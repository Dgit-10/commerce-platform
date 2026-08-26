package com.ecommerce.payment_service.service.impl;

import com.common_packages.common_packages.event.PaymentApprovedEvent;
import com.common_packages.common_packages.event.PaymentProcessedEvent;
import com.common_packages.common_packages.exception.ResourceNotFoundException;
import com.ecommerce.payment_service.dto.PaymentRequest;
import com.ecommerce.payment_service.dto.PaymentResponse;
import com.ecommerce.payment_service.entity.Payment;
import com.ecommerce.payment_service.entity.PaymentStatus;
import com.ecommerce.payment_service.kafka.PaymentEventProducer;
import com.ecommerce.payment_service.repository.PaymentRepository;
import com.ecommerce.payment_service.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // Step 1: Initialize payment record in AWAITING_APPROVAL state
        String txnId = "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseGet(() -> new Payment(
                        request.getOrderId(),
                        request.getUserId(),
                        request.getAmount(),
                        PaymentStatus.AWAITING_APPROVAL,
                        txnId,
                        null
                ));

        payment.setStatus(PaymentStatus.AWAITING_APPROVAL);
        payment.setTransactionId(txnId);
        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment initialized in AWAITING_APPROVAL state for Order ID: {}, Payment ID: {}, Txn: {}",
                savedPayment.getOrderId(), savedPayment.getId(), txnId);

        return mapToResponse(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponse approvePayment(Long paymentId, String approvedBy) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for ID: " + paymentId));

        if (payment.getStatus() == PaymentStatus.APPROVED) {
            log.info("Payment ID {} already approved.", paymentId);
            return mapToResponse(payment);
        }

        payment.setStatus(PaymentStatus.APPROVED);
        payment.setApprovedBy(approvedBy != null ? approvedBy : "SYSTEM");
        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment ID {} APPROVED by {}. Emitting PaymentApprovedEvent...", paymentId, savedPayment.getApprovedBy());

        // Emit verified PaymentApprovedEvent
        PaymentApprovedEvent approvedEvent = new PaymentApprovedEvent(
                savedPayment.getId(),
                savedPayment.getOrderId(),
                savedPayment.getUserId(),
                savedPayment.getAmount(),
                savedPayment.getTransactionId(),
                savedPayment.getApprovedBy(),
                LocalDateTime.now()
        );
        paymentEventProducer.publishPaymentApprovedEvent(approvedEvent);

        // Also emit PaymentProcessedEvent for backward compatibility
        PaymentProcessedEvent processedEvent = new PaymentProcessedEvent(
                savedPayment.getOrderId(),
                savedPayment.getUserId(),
                true,
                savedPayment.getTransactionId(),
                null
        );
        paymentEventProducer.publishPaymentProcessedEvent(processedEvent);

        return mapToResponse(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponse approvePaymentByOrderId(Long orderId, String approvedBy) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for Order ID: " + orderId));
        return approvePayment(payment.getId(), approvedBy);
    }

    @Override
    @Transactional
    public PaymentResponse rejectPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for ID: " + paymentId));

        payment.setStatus(PaymentStatus.REJECTED);
        payment.setFailureReason(reason != null ? reason : "Payment rejected by reviewer/system");
        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment ID {} REJECTED. Reason: {}. Emitting PaymentProcessedEvent (failure)...", paymentId, reason);

        PaymentProcessedEvent processedEvent = new PaymentProcessedEvent(
                savedPayment.getOrderId(),
                savedPayment.getUserId(),
                false,
                savedPayment.getTransactionId(),
                savedPayment.getFailureReason()
        );
        paymentEventProducer.publishPaymentProcessedEvent(processedEvent);

        return mapToResponse(savedPayment);
    }

    @Override
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for ID: " + id));
        return mapToResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for Order ID: " + orderId));
        return mapToResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPendingPayments() {
        return paymentRepository.findByStatus(PaymentStatus.AWAITING_APPROVAL).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
