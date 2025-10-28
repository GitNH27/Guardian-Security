package com.GuardianSecurity.security_backend.controller;

import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.dto.request.LoginRequest;
import com.GuardianSecurity.security_backend.dto.request.RegisterRequest;
import com.GuardianSecurity.security_backend.dto.response.AuthResponse;
import com.GuardianSecurity.security_backend.dto.response.UserResponse;
import com.GuardianSecurity.security_backend.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        User registeredUser = authService.register(request);

        UserResponse userResponse = new UserResponse(
                registeredUser.getId(),
                registeredUser.getEmail(),
                registeredUser.getFirstName(),
                registeredUser.getLastName()
        );

        // Return the user response with CREATED status
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.generateAuthResponse(request);
        return ResponseEntity.ok(authResponse);
    }

}
