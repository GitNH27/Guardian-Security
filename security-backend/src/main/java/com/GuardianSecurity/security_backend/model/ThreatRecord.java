package com.GuardianSecurity.security_backend.model;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data; 
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data 
@NoArgsConstructor 
@Entity 
@Table(name = "threat_records")
public class ThreatRecord {

    // --- MAPPING 1: The Foreign Key Relationship ---
    // This defines the device_id column and handles the integrity.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    // --- MAPPING 2: The Raw String ID for Ingestion (The Fix) ---
    // We map the raw ID string to the same device_id column, 
    // but tell Hibernate NOT to use it for inserts/updates, as the 'device' object handles that.
    @Column(name = "device_id", insertable = false, updatable = false)
    private Long rawDeviceId;
    
    // --- Core ML Data Fields ---
    
    @Column(name = "camera_topic")
    private String cameraTopic; 
    
    @Column(name = "object_detected")
    private String objectDetected; 
    
    @Column(name = "threat_level", nullable = false)
    private String threatLevel; 

    // --- Evidence & Metadata ---
    
    @Column(name = "photo_url", length = 512)
    private String photoUrl;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; 
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Transient field liveStreamUrl
    @Transient
    private String liveStreamUrl;

    // --- Custom JPA Lifecycle Callbacks ---
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (processedAt == null) {
            processedAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
}