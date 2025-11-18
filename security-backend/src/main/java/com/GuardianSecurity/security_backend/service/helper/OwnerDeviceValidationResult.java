package com.GuardianSecurity.security_backend.service.helper;

import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.model.DeviceAccess;

public class OwnerDeviceValidationResult {
    private final User ownerUser;
    private final DeviceAccess deviceAccess;

    public OwnerDeviceValidationResult(User ownerUser, DeviceAccess deviceAccess) {
        this.ownerUser = ownerUser;
        this.deviceAccess = deviceAccess;
    }

    public User getOwnerUser() {
        return ownerUser;
    }

    public DeviceAccess getDeviceAccess() {
        return deviceAccess;
    }
}