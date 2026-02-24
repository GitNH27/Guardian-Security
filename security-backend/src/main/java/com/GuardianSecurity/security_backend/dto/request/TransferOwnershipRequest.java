package com.GuardianSecurity.security_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class TransferOwnershipRequest {

    @NotBlank(message = "Device serial number is required")
    private String deviceSerialNumber;

    @NotBlank(message = "New owner email is required")
    @Email(message = "Invalid email format")
    private String newOwnerEmail;

    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    public void setDeviceSerialNumber(String deviceSerialNumber) {
        this.deviceSerialNumber = deviceSerialNumber;
    }

    public String getNewOwnerEmail() {
        return newOwnerEmail;
    }

    public void setNewOwnerEmail(String newOwnerEmail) {
        this.newOwnerEmail = newOwnerEmail;
    }
}