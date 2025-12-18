package com.GuardianSecurity.security_backend.dto.request;
import jakarta.validation.constraints.NotNull;

public class OwnerDecisionRequest {

    @NotNull(message = "Request ID is required")
    private Long requestId;

    @NotNull(message = "Decision is required")
    private Decision decision;

    public static enum Decision {
        APPROVED,
        REJECTED
    }

    public Long getRequestId() {
        return requestId;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

}