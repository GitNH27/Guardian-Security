package com.GuardianSecurity.security_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates Getters, Setters, toString, equals, and hashCode
@NoArgsConstructor // Generates the no-argument constructor required by JPA
@Entity
@Table(
    name = "device_access",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_id"})
)
public class DeviceAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // IMPORTANT: FetchType.LAZY means these entities are only loaded when accessed.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role = Role.OWNER;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Custom Convenience Constructor (FUNCTIONALITY RETAINED) ---
    // Lombok does NOT interfere with custom constructors.
    public DeviceAccess(User user, Device device, Role role) {
        this.user = user;
        this.device = device;
        this.role = role == null ? Role.OWNER : role;
    }

    // --- Custom JPA Lifecycle Callbacks (FUNCTIONALITY RETAINED) ---
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

    // --- Inner Enum (FUNCTIONALITY RETAINED) ---
    public enum Role {
        OWNER,
        MEMBER
    }
}