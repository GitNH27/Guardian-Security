package com.GuardianSecurity.security_backend.dto.response;

import com.GuardianSecurity.security_backend.model.DeviceAccessPermission;

public class DecisionOnMember {
    private Long requestId;
    private String requesterEmail;
    private String deviceSerial;
    private String ownerEmail;
    private String status;

    public DecisionOnMember(DeviceAccessPermission permission) {
        this.requestId = permission.getId();
        this.requesterEmail = permission.getRequester().getEmail();
        this.deviceSerial = permission.getDevice().getSerialNumber();
        this.ownerEmail = permission.getOwner().getEmail();
        this.status = permission.getStatus().name();
    }

    public Long getRequestId() {
        return requestId;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public String getStatus() {
        return status;
    }
}
