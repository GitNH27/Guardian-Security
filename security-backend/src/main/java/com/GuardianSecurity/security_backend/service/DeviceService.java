package com.GuardianSecurity.security_backend.service;

import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import com.GuardianSecurity.security_backend.model.DeviceAccess.Role;

import com.GuardianSecurity.security_backend.model.Device;
import com.GuardianSecurity.security_backend.model.Device.DeviceStatus;
import com.GuardianSecurity.security_backend.model.DeviceAccess;
import com.GuardianSecurity.security_backend.repository.DeviceRepository;
import com.GuardianSecurity.security_backend.repository.DeviceAccessRepository;
import com.GuardianSecurity.security_backend.dto.request.DeviceClaimRequest;
import com.GuardianSecurity.security_backend.dto.request.AccessDeviceRequest;
import com.GuardianSecurity.security_backend.dto.request.OwnerDecisionRequest;
import com.GuardianSecurity.security_backend.dto.request.OwnerDecisionRequest.Decision;
import com.GuardianSecurity.security_backend.model.DeviceAccessPermission;
import com.GuardianSecurity.security_backend.model.DeviceAccessPermission.Status;
import com.GuardianSecurity.security_backend.repository.DeviceAccessPermissionRepository;

import com.GuardianSecurity.security_backend.service.helper.OwnerDeviceValidationResult;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    // Method to get current user
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private OwnerDeviceValidationResult validateOwnerAndDevice(AccessDeviceRequest accessDeviceRequest) {
        // Check input email is owner of device (current have user email and need there corresponding id)
        String owner = accessDeviceRequest.getOwnerEmail();
        User ownerUser = userRepository.findByEmail(owner)
                          .orElseThrow(() -> new IllegalArgumentException("Owner not found."));
        
        // Get user if from owner user
        Long ownerId = ownerUser.getId();
        DeviceAccess deviceAccess = deviceAccessRepository.findByUserIdAndDeviceSerialNumber(ownerId, accessDeviceRequest.getSerialNumber())
                          .orElseThrow(() -> new IllegalArgumentException("Device not found."));

        return new OwnerDeviceValidationResult(ownerUser, deviceAccess);
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
        User user = getCurrentUser();

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
        User user = getCurrentUser();

        // Check if user is owner of device
        OwnerDeviceValidationResult ownerDeviceValidationResult = validateOwnerAndDevice(accessDeviceRequest);
        User ownerUser = ownerDeviceValidationResult.getOwnerUser();
        DeviceAccess deviceAccess = ownerDeviceValidationResult.getDeviceAccess();

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

    // Method to approve access request
    @Transactional
    public DeviceAccessPermission decisionOnMember(OwnerDecisionRequest ownerDecisionRequest) {
        // Get current user
        User user = getCurrentUser();

        // Retrieve the Access Request
        DeviceAccessPermission accessRequest = deviceAccessPermissionRepository.findById(ownerDecisionRequest.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Access request not found."));

        // Confirm current user is the owner
        if(!accessRequest.getOwner().getEmail().equals(user.getEmail())) {
            throw new IllegalArgumentException("User is not the owner of the device.");
        }

        if (ownerDecisionRequest.getDecision() == Decision.APPROVED) {
            // Set status to APPROVED
            accessRequest.setStatus(Status.APPROVED);
            deviceAccessPermissionRepository.save(accessRequest);

            // Create new device access
            DeviceAccess newAccess = new DeviceAccess(accessRequest.getRequester(), accessRequest.getDevice(), Role.MEMBER);
            deviceAccessRepository.save(newAccess);

        } else {
            accessRequest.setStatus(Status.REJECTED);
            deviceAccessPermissionRepository.save(accessRequest);
        }
        return accessRequest;
    }

    // Devices owned or accessible by the authenticated user.
    // Goal: Check devices. if none -> Redirect to claim device page. if one -> Redirect to device page, if more -> Redirect to device list page
    public Map<String, Object> getDeviceSelectionContext() {
        User user = getCurrentUser();
        
        List<DeviceAccess> accessList = deviceAccessRepository.findAllByUserId(user.getId());

        Map<String, Object> context = new HashMap<>();
        
        if (accessList.isEmpty()) {
            context.put("action", "Redirect-Claim-Request"); // React Native will show "Pair/Request"
        } else if (accessList.size() == 1) {
            context.put("action", "Auto-Redirect-Device");
            context.put("device", convertToSelectionDto(accessList.get(0)));
        } else {
            context.put("action", "Select-Device");
            context.put("devices", accessList.stream()
                    .map(this::convertToSelectionDto)
                    .collect(Collectors.toList()));
        }
        
        return context;
    }

    private Map<String, Object> convertToSelectionDto(DeviceAccess access) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("deviceId", access.getDevice().getId());
        dto.put("serialNumber", access.getDevice().getSerialNumber());
        dto.put("role", access.getRole().name());
        dto.put("status", access.getDevice().getStatus().name());
        return dto;
    }
}