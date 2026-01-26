package com.GuardianSecurity.security_backend.service;

import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.model.UserFcmToken;
import com.GuardianSecurity.security_backend.repository.FcmTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FcmTokenService {

    @Autowired
    private FcmTokenRepository fcmTokenRepository;

    @Transactional
    public void saveOrUpdateToken(User user, String token, String deviceName) {
        // 1. Check if this specific token already exists
        Optional<UserFcmToken> existingToken = fcmTokenRepository.findByToken(token);

        if (existingToken.isPresent()) {
            UserFcmToken fcmToken = existingToken.get();
            // Update timestamp and device name if changed
            fcmToken.setLastUsed(LocalDateTime.now());
            fcmToken.setDeviceName(deviceName);
            // Ensure it's linked to the current user (in case of account switch on same device)
            fcmToken.setUser(user);
            fcmTokenRepository.save(fcmToken);
        } else {
            // 2. Create a brand new token record for this device
            UserFcmToken newToken = new UserFcmToken();
            newToken.setUser(user);
            newToken.setToken(token);
            newToken.setDeviceName(deviceName);
            newToken.setLastUsed(LocalDateTime.now());
            fcmTokenRepository.save(newToken);
        }
    }

    @Transactional
    public void removeToken(User user, String token) {
        Optional<UserFcmToken> existingToken = fcmTokenRepository.findByToken(token);

        if (existingToken.isPresent() && existingToken.get().getUser().getId().equals(user.getId())) {
            fcmTokenRepository.deleteByToken(token);
        } else {
            throw new RuntimeException("Unauthorized attempt to remove FCM token");
        }
    }
}