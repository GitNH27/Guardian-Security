package com.GuardianSecurity.security_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data; // Provides Getters, Setters, toString, equals, hashCode
import lombok.NoArgsConstructor; // Provides the default no-arg constructor required by JPA
// import lombok.AllArgsConstructor; // Omit AllArgsConstructor since you have a custom constructor

@Data // <-- The core Lombok annotation
@NoArgsConstructor // <-- JPA requirement
@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", nullable = false, unique = true, length = 255)
    private String serialNumber;

    @Column(name = "pairing_password", length = 12)
    private String pairingPassword;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private DeviceStatus status = DeviceStatus.UNCLAIMED;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Custom Constructor (Keep this as you need specific initialization)
    public Device(String serialNumber, String pairingPassword) {
        this.serialNumber = serialNumber;
        this.pairingPassword = pairingPassword;
        this.status = DeviceStatus.UNCLAIMED;
    }

    // --- Custom JPA Lifecycle Callbacks (Keep These) ---
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // --- Inner Enum (Keep This) ---
    public enum DeviceStatus {
        UNCLAIMED,
        CLAIMED
    }
}