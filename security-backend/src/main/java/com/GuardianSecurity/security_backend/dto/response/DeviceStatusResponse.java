package com.GuardianSecurity.security_backend.dto.response;

import java.time.LocalDateTime;

public class DeviceStatusResponse {
    private Long deviceId;
    private String status; 
    private String message;
    private LocalDateTime lastSeen;

    public DeviceStatusResponse(Long deviceId, String status, String message, LocalDateTime lastSeen) {
        this.deviceId = deviceId;
        this.status = status;
        this.message = message;
        this.lastSeen = lastSeen;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }
}
