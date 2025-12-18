package com.GuardianSecurity.security_backend.dto.internal;

public class MlDataPayload {

    // No-arg constructor required by Jackson for deserialization
    public MlDataPayload() {
    }

    private String level;
    private String object;
    private String url;

    // --- Getters ---
    public String getLevel() {
        return level;
    }

    public String getObject() {
        return object;
    }

    public String getUrl() {
        return url;
    }

    // --- Setters ---
    public void setLevel(String level) {
        this.level = level;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}