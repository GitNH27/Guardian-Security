package com.GuardianSecurity.security_backend.service;
import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.repository.UserRepository;
import com.GuardianSecurity.security_backend.dto.request.UpdatePasswordRequest;
import com.GuardianSecurity.security_backend.dto.request.UpdateUserRequest;
import org.springframework.stereotype.Service;

import com.GuardianSecurity.security_backend.dto.response.UserResponse;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse updateUser(Long id, UpdateUserRequest userRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if ((userRequest.getFirstName() != null) && (!userRequest.getFirstName().equals(existingUser.getFirstName()))) {
            existingUser.setFirstName(userRequest.getFirstName());
        }
        if ((userRequest.getLastName() != null) && (!userRequest.getLastName().equals(existingUser.getLastName()))) {
            existingUser.setLastName(userRequest.getLastName());
        }
        if ((userRequest.getEmail() != null) && (!userRequest.getEmail().equals(existingUser.getEmail()))) {
            existingUser.setEmail(userRequest.getEmail());
        }
        
        User savedUser = userRepository.save(existingUser);
        
        // Map Entity to DTO
        return new UserResponse(
            savedUser.getId(), 
            savedUser.getEmail(), 
            savedUser.getFirstName(), 
            savedUser.getLastName()
        );
    }

    public UserResponse updateUserPassword(Long id, UpdatePasswordRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (passwordEncoder.matches(request.getOldPassword(), existingUser.getPasswordHash())) {
            if (request.getNewPassword().equals(request.getConfirmPassword())) {
                existingUser.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                User savedUser = userRepository.save(existingUser);
                
                return new UserResponse(
                    savedUser.getId(), 
                    savedUser.getEmail(), 
                    savedUser.getFirstName(), 
                    savedUser.getLastName()
                );
            } else {
                throw new IllegalArgumentException("New password and confirmation do not match");
            }
        } else {
            throw new IllegalArgumentException("Old password is incorrect");
        }
    }

}
