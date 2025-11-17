package com.GuardianSecurity.security_backend.dto.request;
import jakarta.validation.constraints.NotBlank;

public class DeviceClaimRequest {
    @NotBlank(message = "Code is required")
    private String deviceCode;

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

}
