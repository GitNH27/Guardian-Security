package com.GuardianSecurity.security_backend.controller;

import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.model.Device;
import com.GuardianSecurity.security_backend.model.DeviceAccess;

import com.GuardianSecurity.security_backend.service.DeviceService;

import com.GuardianSecurity.security_backend.dto.request.DeviceClaimRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
}
