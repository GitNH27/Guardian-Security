package com.GuardianSecurity.security_backend.dto.response;

import java.time.LocalDateTime;

public class ThreatLogResponse {
    private Long id;
    private String threatLevel;
    private String objectDetected;
    private String cameraTopic;
    private String photoUrl;
    private LocalDateTime timestamp;

    // IMPORTANT: You need this constructor
    public ThreatLogResponse(Long id, String threatLevel, String objectDetected, String cameraTopic, String photoUrl, LocalDateTime timestamp) {
        this.id = id;
        this.threatLevel = threatLevel;
        this.objectDetected = objectDetected;
        this.cameraTopic = cameraTopic;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
    }

    // JACKSON NEEDS THESE GETTERS
    public Long getId() { return id; }
    public String getThreatLevel() { return threatLevel; }
    public String getObjectDetected() { return objectDetected; }
    public String getCameraTopic() { return cameraTopic; }
    public String getPhotoUrl() { return photoUrl; }
    public LocalDateTime getTimestamp() { return timestamp; }
}