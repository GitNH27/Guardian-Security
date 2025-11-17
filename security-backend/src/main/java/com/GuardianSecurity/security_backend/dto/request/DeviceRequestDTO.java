package com.GuardianSecurity.security_backend.dto.request;

// Import the entity class from the correct package
import com.GuardianSecurity.security_backend.model.DeviceAccessPermission;

public class DeviceRequestDTO {

    private Long id;
    private String requesterEmail;
    private String deviceSerial;
    private String ownerEmail;
    private String status;

    public DeviceRequestDTO(DeviceAccessPermission request) {
        this.id = request.getId();
        this.requesterEmail = request.getRequester().getEmail();
        this.deviceSerial = request.getDevice().getSerialNumber();
        this.ownerEmail = request.getOwner().getEmail();
        this.status = request.getStatus().name();
    }

    //Getters
    public Long getId() {
        return id;
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
