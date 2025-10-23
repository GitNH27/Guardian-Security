// Edit for request on what device needs to pair

package com.GuardianSecurity.security_backend.dto.request;

public class DevicePairRequest {
    private String deviceName;
    private String deviceType;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
