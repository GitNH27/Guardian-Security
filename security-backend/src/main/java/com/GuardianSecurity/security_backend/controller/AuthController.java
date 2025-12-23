package com.GuardianSecurity.security_backend.controller;

import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.dto.request.LoginRequest;
import com.GuardianSecurity.security_backend.dto.request.RegisterRequest;
import com.GuardianSecurity.security_backend.dto.response.AuthResponse;
import com.GuardianSecurity.security_backend.dto.response.UserResponse;
import com.GuardianSecurity.security_backend.dto.response.VerifyUserResponse;
import com.GuardianSecurity.security_backend.service.AuthService;

import com.GuardianSecurity.security_backend.service.VerifyEmail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*") // Allow requests from any origin
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
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/test")
    public boolean test() {
        return passwordEncoder.matches("N_hummer27", 
            "$2a$10$WGsgkNQ6UjULGpbu7bJR/eZAPsm900gz3NiOmLgqOlB.DdRt8M1PG"); // paste hash from DB
    }

}
