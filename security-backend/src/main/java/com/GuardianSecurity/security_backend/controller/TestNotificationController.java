package com.GuardianSecurity.security_backend.controller;

import com.GuardianSecurity.security_backend.model.User;
import com.GuardianSecurity.security_backend.service.FcmNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/notifications")
public class TestNotificationController {

    private final FcmNotificationService fcmNotificationService;

    public TestNotificationController(FcmNotificationService fcmNotificationService) {
        this.fcmNotificationService = fcmNotificationService;
    }

    /**
     * Simulates an INTRUDER alert.
     * Expectation: Phone rings/vibrates, and app shows a RED Danger UI.
     */
    @PostMapping("/intruder")
    public ResponseEntity<String> simulateIntruder(@AuthenticationPrincipal User user) {
        fcmNotificationService.sendThreatNotification(
            user, 
            "IMMINENT THREAT", 
            "Unauthorized person detected inside the vehicle!", 
            true // isIntruder = true
        );
        return ResponseEntity.ok("Intruder simulation dispatched to " + user.getEmail());
    }

    /**
     * Simulates a HIGH THREAT log.
     * Expectation: Normal notification, app shows standard log update.
     */
    @PostMapping("/high-alert")
    public ResponseEntity<String> simulateHighAlert(@AuthenticationPrincipal User user) {
        fcmNotificationService.sendThreatNotification(
            user, 
            "Security Alert", 
            "Suspicious activity detected near the car.", 
            false // isIntruder = false
        );
        return ResponseEntity.ok("High alert simulation dispatched to " + user.getEmail());
    }
}