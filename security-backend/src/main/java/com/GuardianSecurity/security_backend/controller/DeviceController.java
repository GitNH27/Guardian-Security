package com.GuardianSecurity.security_backend.controller;

import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.model.Device;
import com.GuardianSecurity.security_backend.model.DeviceAccess;
import com.GuardianSecurity.security_backend.model.DeviceAccessPermission;

import com.GuardianSecurity.security_backend.service.DeviceService;

import com.GuardianSecurity.security_backend.dto.request.DeviceClaimRequest;
import com.GuardianSecurity.security_backend.dto.response.AccessDeviceResponse;
import com.GuardianSecurity.security_backend.dto.request.AccessDeviceRequest;
import com.GuardianSecurity.security_backend.dto.request.OwnerDecisionRequest;
import com.GuardianSecurity.security_backend.dto.response.DecisionOnMember;
import com.GuardianSecurity.security_backend.service.helper.OwnerDeviceValidationResult;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    private final DeviceService deviceService;


    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    // Method to claim a device (recieves pairing_password from user) needs JWT authentication
    @PostMapping("/claim")
    public ResponseEntity<Device> claimDevice(@Valid @RequestBody DeviceClaimRequest deviceClaimRequest) {
        Device device = deviceService.claimDevice(deviceClaimRequest);  // Claim the device
        return ResponseEntity.status(HttpStatus.CREATED).body(device);  // Return the claimed device
    }

    // Method to request access to a device (input serial number and owner email)
    @PostMapping("/requestAccess")
    public ResponseEntity<AccessDeviceResponse> requestAccess(@Valid @RequestBody AccessDeviceRequest accessDeviceRequest) {

        DeviceAccessPermission savedRequest = deviceService.requestAccess(accessDeviceRequest);

        // Convert to DTO for safe JSON serialization
        AccessDeviceResponse responseDto = new AccessDeviceResponse(savedRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // Method to approve or deny a member's access request
    @PostMapping("/memberAccessDecision")
    public ResponseEntity<DecisionOnMember> memberAccessDecision(@Valid @RequestBody OwnerDecisionRequest ownerDecisionRequest) {
        DeviceAccessPermission savedRequest = deviceService.decisionOnMember(ownerDecisionRequest); // Approve or deny request

        // Convert to DTO for safe JSON serialization
        DecisionOnMember responseDto = new DecisionOnMember(savedRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

    }

    // Devices owned or accessible by the authenticated user (For page redirection logic - depending on number of devices)
    @GetMapping("/myDevices")
    public ResponseEntity<Map<String, Object>> getDeviceSelectionContext() {
        Map<String, Object> context = deviceService.getDeviceSelectionContext();
        return ResponseEntity.status(HttpStatus.OK).body(context);
    }

}