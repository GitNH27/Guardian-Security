package com.GuardianSecurity.security_backend.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AccessDeviceRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String ownerEmail;

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

}