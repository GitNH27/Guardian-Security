package com.GuardianSecurity.security_backend.controller;

import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.dto.request.UpdateUserRequest;
import com.GuardianSecurity.security_backend.dto.request.TransferOwnershipRequest;
import com.GuardianSecurity.security_backend.dto.request.UpdatePasswordRequest;
import com.GuardianSecurity.security_backend.service.UserService;
import com.GuardianSecurity.security_backend.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final DeviceService deviceService;

    public UserController(UserService userService, DeviceService deviceService) {
        this.userService = userService;
        this.deviceService = deviceService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody @Valid UpdateUserRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).build();
        }

        User updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<User> updateUserPassword(@PathVariable Long id, @RequestBody @Valid UpdatePasswordRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).build();
        }

        User updatedUser = userService.updateUserPassword(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{id}/device/transfer")
    public ResponseEntity<Void> transferOwnership(@PathVariable Long id, @RequestBody @Valid TransferOwnershipRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        // Ensure the authenticated user is the one associated with the ID in the path
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).build();
        }

        deviceService.transferDeviceOwnership(request);
        return ResponseEntity.ok().build();
    }

}
