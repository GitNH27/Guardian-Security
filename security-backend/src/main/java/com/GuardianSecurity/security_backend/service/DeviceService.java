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
import com.GuardianSecurity.security_backend.model.DeviceAccessPermission;
import com.GuardianSecurity.security_backend.model.DeviceAccessPermission.Status;
import com.GuardianSecurity.security_backend.repository.DeviceAccessPermissionRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceAccessRepository deviceAccessRepository;
    private final DeviceAccessPermissionRepository deviceAccessPermissionRepository;


    public DeviceService(UserRepository userRepository, DeviceRepository deviceRepository, DeviceAccessRepository deviceAccessRepository, DeviceAccessPermissionRepository deviceAccessPermissionRepository) {
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.deviceAccessRepository = deviceAccessRepository;
        this.deviceAccessPermissionRepository = deviceAccessPermissionRepository;
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

    // Method to request access to a device (input owner email)
    @Transactional
    public DeviceAccessPermission requestAccess(AccessDeviceRequest accessDeviceRequest) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // Get current user
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                          .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Check input email is owner of device (current have user email and need there corresponding id)
        String owner = accessDeviceRequest.getOwnerEmail();
        User ownerUser = userRepository.findByEmail(owner)
                          .orElseThrow(() -> new IllegalArgumentException("Owner not found."));
        
        // Get user if from owner user
        Long ownerId = ownerUser.getId();
        DeviceAccess deviceAccess = deviceAccessRepository.findByUserIdAndDeviceSerialNumber(ownerId, accessDeviceRequest.getSerialNumber())
                          .orElseThrow(() -> new IllegalArgumentException("Device not found."));

        // Check if user already has access to device
        if(deviceAccessRepository.findByUserIdAndDeviceSerialNumber(user.getId(), deviceAccess.getDevice().getSerialNumber()).isPresent()) {
            throw new IllegalArgumentException("User already has access to device.");
        }

        if(deviceAccessPermissionRepository
                .findByRequester_IdAndDevice_SerialNumber(user.getId(), deviceAccess.getDevice().getSerialNumber())
                .isPresent()) {
            throw new IllegalArgumentException("User already made request.");
        }


        // Device access request update
        DeviceAccessPermission newRequest = new DeviceAccessPermission(user, deviceAccess.getDevice(), ownerUser, Status.PENDING);
        deviceAccessPermissionRepository.save(newRequest);

        return newRequest;
    }

}
