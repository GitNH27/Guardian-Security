package com.GuardianSecurity.security_backend.service;
import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.repository.UserRepository;
import com.GuardianSecurity.security_backend.dto.request.UpdatePasswordRequest;
import com.GuardianSecurity.security_backend.dto.request.UpdateUserRequest;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User updateUser(Long id, UpdateUserRequest user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if ((user.getFirstName() != null) && (!user.getFirstName().equals(existingUser.getFirstName()))) {
            existingUser.setFirstName(user.getFirstName());
        }
        if ((user.getLastName() != null) && (!user.getLastName().equals(existingUser.getLastName()))) {
            existingUser.setLastName(user.getLastName());
        }
        if ((user.getEmail() != null) && (!user.getEmail().equals(existingUser.getEmail()))) {
            existingUser.setEmail(user.getEmail());
        }
        return userRepository.save(existingUser);
    }

    // Password update and validation logic
    public User updateUserPassword(Long id, UpdatePasswordRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (passwordEncoder.matches(request.getOldPassword(), existingUser.getPasswordHash())) {
            if (request.getNewPassword().equals(request.getConfirmPassword())) {
                existingUser.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                return userRepository.save(existingUser);
            } else {
                throw new IllegalArgumentException("New password and confirmation do not match");
            }
        } else {
            throw new IllegalArgumentException("Old password is incorrect");
        }
    }

}
