package com.GuardianSecurity.security_backend.controller;

import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.dto.request.LoginRequest;
import com.GuardianSecurity.security_backend.dto.request.RegisterRequest;
import com.GuardianSecurity.security_backend.dto.response.AuthResponse;
import com.GuardianSecurity.security_backend.dto.response.UserResponse;
import com.GuardianSecurity.security_backend.dto.response.VerifyUserResponse;
import com.GuardianSecurity.security_backend.service.AuthService;
import com.GuardianSecurity.security_backend.dto.request.FcmTokenRequest;
import com.GuardianSecurity.security_backend.service.FcmTokenService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final FcmTokenService fcmTokenService;
    
    public AuthController(AuthService authService, VerifyEmail verifyEmail, FcmTokenService fcmTokenService) {
        this.authService = authService;
        this.verifyEmail = verifyEmail;
        this.fcmTokenService = fcmTokenService;
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

    @PostMapping("/register-fcm")
    public ResponseEntity<String> registerFcmToken(
            @Valid @RequestBody FcmTokenRequest request, 
            @AuthenticationPrincipal User user) { // 2. Get the current user automatically
        
        // 3. Link the token to the user found in the security context
        fcmTokenService.saveOrUpdateToken(user, request.getToken(), request.getDeviceName());
        
        return ResponseEntity.status(HttpStatus.OK).body("Device successfully registered for security alerts");
    }

    @PostMapping("/logout-fcm")
    public ResponseEntity<String> removeFcmToken(
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal User user) {
        
        String token = body.get("token");
        fcmTokenService.removeToken(user, token);
        
        return ResponseEntity.ok("Device unregistered successfully");
    }

}
