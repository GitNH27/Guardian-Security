package com.GuardianSecurity.security_backend.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AccessDeviceRequest {

    @NotBlank(message = "Device Serial Number is required")
    private String serialNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String ownerEmail;

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

}