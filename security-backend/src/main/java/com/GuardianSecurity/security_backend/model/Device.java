package com.GuardianSecurity.security_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
public class Device {

    // No-argument constructor just for JPA
    public Device() {
    }

    // Default constructor
    public Device(String serialNumber , String pairingPassword) {
        this.serialNumber = serialNumber;
        this.pairingPassword = pairingPassword;
        this.status = DeviceStatus.UNCLAIMED;
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getPairingPassword() {
        return pairingPassword;
    }

    public void setPairingPassword(String pairingPassword) {
        this.pairingPassword = pairingPassword;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

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

    public enum DeviceStatus {
        UNCLAIMED,
        CLAIMED
    }
}