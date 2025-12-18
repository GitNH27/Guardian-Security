package com.GuardianSecurity.security_backend.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates Getters, Setters, toString, equals, and hashCode
@NoArgsConstructor // Generates the no-argument constructor required by JPA
@Entity
@Table(
    name = "device_access_requests",
    uniqueConstraints = @UniqueConstraint(columnNames = {"requester_id", "device_id"}, name = "unique_request")
)
public class DeviceAccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user requesting access (FK to users.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // The device being requested (FK to devices.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    // The current owner who must approve the request (FK to users.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    // Status of the request: PENDING, APPROVED, REJECTED
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // --- Custom Convenience Constructor ---
    public DeviceAccessRequest(User requester, Device device, User owner) {
        this.requester = requester;
        this.device = device;
        this.owner = owner;
    }

    // --- Custom JPA Lifecycle Callbacks ---
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // --- Inner Enum ---
    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}