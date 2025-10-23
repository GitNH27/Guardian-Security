package com.GuardianSecurity.security_backend.service;

import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.repository.UserRepository;
import com.GuardianSecurity.security_backend.security.JwtSecurityTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.GuardianSecurity.security_backend.dto.request.RegisterRequest;
import com.GuardianSecurity.security_backend.dto.request.LoginRequest;
import com.GuardianSecurity.security_backend.dto.response.AuthResponse;
import com.GuardianSecurity.security_backend.dto.response.UserResponse;

import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtSecurityTask jwtSecurityTask;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtSecurityTask jwtSecurityTask) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtSecurityTask = jwtSecurityTask;
    }

    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        User newUser = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName()
        );

        User savedUser = userRepository.save(newUser);

        return savedUser;
    }

    // Authenticate user and return User object
    public User loginAuthenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return user;
    }

    // Generate JWT token for authenticated user after login
    public AuthResponse generateAuthResponse(LoginRequest request) {
        User user = loginAuthenticate(request);

        String token = jwtSecurityTask.generateToken(user);

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );

        AuthResponse authResponse = new AuthResponse(token, userResponse);
        return authResponse;
    }
}