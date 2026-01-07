package com.GuardianSecurity.security_backend.dto.response;
import java.util.Map;

public class LiveFeedResponse {
    // Example: { "car/ml/front": "http://...", "car/ml/back": "http://..." }
    private Map<String, String> activeFeeds;

    public LiveFeedResponse(Map<String, String> activeFeeds) {
        this.activeFeeds = activeFeeds;
    }

    public Map<String, String> getActiveFeeds() {
        return activeFeeds;
    }
}