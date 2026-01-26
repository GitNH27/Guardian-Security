package com.GuardianSecurity.security_backend.service;

import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.model.UserFcmToken;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FcmNotificationService {
    
    // Adjusted signature to accept dynamic Title, Body, and the isIntruder flag
    public void sendThreatNotification(User user, String title, String body, boolean isIntruder) {
        List<String> tokens = user.getFcmTokens().stream()
                .map(UserFcmToken::getToken)
                .collect(Collectors.toList());

        if (tokens.isEmpty()) {
            log.info("No devices registered for user: {}. Skipping notification.", user.getEmail());
            return;
        }

        // Build the Multicast message
        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH) // Wake up the phone instantly
                        .build())
                // DATA PAYLOAD: This is what the React Native code reads in the background
                .putData("screen", "ActivityLogScreen")
                .putData("isIntruder", String.valueOf(isIntruder)) // "true" or "false"
                .putData("priority", isIntruder ? "CRITICAL" : "NORMAL")
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("Sent {} notifications to {}. Intruder Mode: {}", 
                     response.getSuccessCount(), user.getEmail(), isIntruder);
        } catch (FirebaseMessagingException e) {
            log.error("Firebase error for user {}", user.getEmail(), e);
        }
    }
}