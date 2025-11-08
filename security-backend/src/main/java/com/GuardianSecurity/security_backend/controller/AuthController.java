package com.GuardianSecurity.security_backend.controller;

import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.dto.request.LoginRequest;
import com.GuardianSecurity.security_backend.dto.request.RegisterRequest;
import com.GuardianSecurity.security_backend.dto.response.AuthResponse;
import com.GuardianSecurity.security_backend.dto.response.UserResponse;
import com.GuardianSecurity.security_backend.dto.response.VerifyUserResponse;
import com.GuardianSecurity.security_backend.service.AuthService;

import com.GuardianSecurity.security_backend.service.VerifyEmail;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final VerifyEmail verifyEmail;

    public AuthController(AuthService authService, VerifyEmail verifyEmail) {
        this.authService = authService;
        this.verifyEmail = verifyEmail;
    }

    @PostMapping("/verifycode")
    public ResponseEntity<VerifyUserResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        // Generate verification code
        String verificationCode = verifyEmail.generateVerificationCode();

        // Store unverified user with code
        authService.storeUnverifiedUser(request, verificationCode);

        // Send verification email
        verifyEmail.sendVerificationEmail(request.getEmail(), verificationCode);

        return ResponseEntity.status(HttpStatus.CREATED).body(new VerifyUserResponse("Verification email sent"));
    }

    @PostMapping("/verify-registration")
    public ResponseEntity<UserResponse> verifyRegistration(@Valid @RequestBody RegisterRequest request, @RequestParam String code) {
        // Verify code and register user
        User registeredUser = authService.register(request, code);
        UserResponse userResponse = new UserResponse(registeredUser.getId(), registeredUser.getEmail(), registeredUser.getFirstName(), registeredUser.getLastName());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.generateAuthResponse(request);
        return ResponseEntity.ok(authResponse);
    }

}
