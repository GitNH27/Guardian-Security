package com.GuardianSecurity.security_backend.dto.response;

// Includes the deviceId, serialNumber, and the role for that specific link.
public class DeviceSelectionResponse {
    private Long deviceId;
    private String serialNumber;
    private String role;
    private String status;

    public DeviceSelectionResponse(Long deviceId, String serialNumber, String role, String status) {
        this.deviceId = deviceId;
        this.serialNumber = serialNumber;
        this.role = role;
        this.status = status;
    }
    public Long getDeviceId() {
        return deviceId;
    }

    public String getserialNumber() {
        return serialNumber;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

}
