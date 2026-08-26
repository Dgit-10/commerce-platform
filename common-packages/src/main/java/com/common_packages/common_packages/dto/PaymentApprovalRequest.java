package com.common_packages.common_packages.dto;

import jakarta.validation.constraints.NotBlank;

public class PaymentApprovalRequest {

    private boolean approved;

    @NotBlank(message = "Approver name/ID is required")
    private String approvedBy;

    private String rejectionReason;

    public PaymentApprovalRequest() {}

    public PaymentApprovalRequest(boolean approved, String approvedBy, String rejectionReason) {
        this.approved = approved;
        this.approvedBy = approvedBy;
        this.rejectionReason = rejectionReason;
    }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
