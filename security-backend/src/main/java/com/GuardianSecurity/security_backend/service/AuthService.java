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
import com.GuardianSecurity.security_backend.dto.request.UnverifiedUserRequest;

import org.springframework.data.redis.core.RedisTemplate;
import java.time.Duration;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtSecurityTask jwtSecurityTask;

    // Autowire the configured RedisTemplate
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String UNVERIFIED_USER_PREFIX = "unverified:"; 
    private static final Duration CODE_EXPIRY = Duration.ofHours(1);

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtSecurityTask jwtSecurityTask) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtSecurityTask = jwtSecurityTask;
    }

    // Store unverified user with code in Redis
    public void storeUnverifiedUser(RegisterRequest request, String verificationCode) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        UnverifiedUserRequest unverifiedUser = new UnverifiedUserRequest(request, verificationCode);
        String key = UNVERIFIED_USER_PREFIX + request.getEmail();
        redisTemplate.opsForValue().set(key, unverifiedUser, CODE_EXPIRY);
    }

    // Verify email code and register user
    public User register(RegisterRequest request, String verificationCode) {
        // Retrieve unverified user from Redis
        String key = UNVERIFIED_USER_PREFIX + request.getEmail();

        // Cast the retrieved object to UnverifiedUserRequest
        UnverifiedUserRequest storedUnverifiedUser = (UnverifiedUserRequest) redisTemplate.opsForValue().get(key);

        // Check if user input verification code matches stored code from Redis
        if (!storedUnverifiedUser.getVerificationCode().equals(verificationCode)) {
            throw new RuntimeException("Invalid or expired verification code.");
        }

        // Persist the user to the main database
        User newUser = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName()
        );

        User savedUser = userRepository.save(newUser);

        redisTemplate.delete(key);

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