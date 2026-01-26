package com.GuardianSecurity.security_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_fcm_tokens")
public class UserFcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @PrePersist
    protected void onRegister() {
        lastUsed = LocalDateTime.now();
    }

    public UserFcmToken(User user, String token, String deviceName) {
        this.user = user;
        this.token = token;
        this.deviceName = deviceName;
    }
}