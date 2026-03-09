package com.GuardianSecurity.security_backend.dto.internal;
import java.util.List;

public class MlDataPayload {

    // No-arg constructor required by Jackson for deserialization
    public MlDataPayload() {
    }

    private String level;
    private List<String> objects;
    private String url;
    private String liveFeed;

    // --- Getters ---
    public String getLevel() {
        return level;
    }

    public List<String> getObjects() {
        return objects;
    }

    public String getUrl() {
        return url;
    }

    public String getLiveFeed() {
        return liveFeed;
    }

    // --- Setters ---
    public void setLevel(String level) {
        this.level = level;
    }

    public void setObjects(List<String> objects) {
        this.objects = objects;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setLiveFeed(String liveFeed) {
        this.liveFeed = liveFeed;
    }
}