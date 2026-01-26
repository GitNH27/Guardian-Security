package com.GuardianSecurity.security_backend.dto.request;
import jakarta.validation.constraints.NotNull;

public class FcmTokenRequest {
    @NotNull(message = "FCM token is required")
    private String token;
    @NotNull(message = "Device name is required")
    private String deviceName;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}