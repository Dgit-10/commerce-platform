package com.ecommerce.payment_service.controller;

import com.common_packages.common_packages.dto.ApiResponse;
import com.common_packages.common_packages.dto.PaymentApprovalRequest;
import com.ecommerce.payment_service.dto.PaymentRequest;
import com.ecommerce.payment_service.dto.PaymentResponse;
import com.ecommerce.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment initiated. Status: Awaiting Approval", response));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<PaymentResponse>> approvePayment(
            @PathVariable Long id,
            @RequestBody(required = false) PaymentApprovalRequest approvalRequest) {
        String approvedBy = (approvalRequest != null && approvalRequest.getApprovedBy() != null)
                ? approvalRequest.getApprovedBy()
                : "AUTHORIZED_USER";

        PaymentResponse response = paymentService.approvePayment(id, approvedBy);
        return ResponseEntity.ok(ApiResponse.success("Payment approved successfully", response));
    }

    @PostMapping("/order/{orderId}/approve")
    public ResponseEntity<ApiResponse<PaymentResponse>> approvePaymentByOrderId(
            @PathVariable Long orderId,
            @RequestBody(required = false) PaymentApprovalRequest approvalRequest) {
        String approvedBy = (approvalRequest != null && approvalRequest.getApprovedBy() != null)
                ? approvalRequest.getApprovedBy()
                : "AUTHORIZED_USER";

        PaymentResponse response = paymentService.approvePaymentByOrderId(orderId, approvedBy);
        return ResponseEntity.ok(ApiResponse.success("Payment for order approved successfully", response));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<PaymentResponse>> rejectPayment(
            @PathVariable Long id,
            @RequestBody(required = false) PaymentApprovalRequest rejectionRequest) {
        String reason = (rejectionRequest != null && rejectionRequest.getRejectionReason() != null)
                ? rejectionRequest.getRejectionReason()
                : "Payment declined by user/system";

        PaymentResponse response = paymentService.rejectPayment(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Payment rejected", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", response));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", response));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPendingPayments() {
        List<PaymentResponse> response = paymentService.getPendingPayments();
        return ResponseEntity.ok(ApiResponse.success("Pending approval payments retrieved successfully", response));
    }
}
