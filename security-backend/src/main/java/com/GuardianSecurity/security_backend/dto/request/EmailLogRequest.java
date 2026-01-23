package com.GuardianSecurity.security_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for requesting a threat log report via email.
 */
public record EmailLogRequest(
    @NotNull(message = "Log ID is required") 
    Long logId,

    @NotNull(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    String email
) {
    public Long getlogId() {
        return logId;
    }
    public String getEmail() {
        return email;
    }
}