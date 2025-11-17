package com.GuardianSecurity.security_backend.service;

import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import com.GuardianSecurity.security_backend.model.DeviceAccess.Role;

import com.GuardianSecurity.security_backend.model.Device;
import com.GuardianSecurity.security_backend.model.Device.DeviceStatus;
import com.GuardianSecurity.security_backend.model.DeviceAccess;
import com.GuardianSecurity.security_backend.repository.DeviceRepository;
import com.GuardianSecurity.security_backend.repository.DeviceAccessRepository;
import com.GuardianSecurity.security_backend.dto.request.DeviceClaimRequest;
import com.GuardianSecurity.security_backend.dto.request.AccessDeviceRequest;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceAccessRepository deviceAccessRepository;


    public DeviceService(UserRepository userRepository, DeviceRepository deviceRepository, DeviceAccessRepository deviceAccessRepository) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.deviceAccessRepository = deviceAccessRepository;
    }

    // Method to claim a device (recieves pairing_password from user)
    @Transactional
    public Device claimDevice(DeviceClaimRequest deviceClaimRequest) {
        String pairingPassword = deviceClaimRequest.getDeviceCode();

        Device device = deviceRepository.findByPairingPassword(pairingPassword)
                .orElseThrow(() -> new IllegalArgumentException("Device with pairing password not found."));

        if (device.getStatus().equals(Device.DeviceStatus.CLAIMED)) {  // Handle Device Already Claimed
            throw new IllegalArgumentException("Device is already claimed.");
        }

        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // Get current user
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                          .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Set device status to CLAIMED
        device.setStatus(DeviceStatus.CLAIMED);
        deviceRepository.save(device);

        // Create DeviceAccess record
        DeviceAccess deviceAccess = new DeviceAccess(user, device, Role.OWNER);
        deviceAccessRepository.save(deviceAccess);

        return device;
    }
    
}
