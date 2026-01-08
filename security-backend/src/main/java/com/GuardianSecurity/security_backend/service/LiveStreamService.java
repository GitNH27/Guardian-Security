package com.GuardianSecurity.security_backend.service;
import com.GuardianSecurity.security_backend.dto.response.LiveFeedResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class LiveStreamService {
    private final StringRedisTemplate stringRedisTemplate;
    private final DeviceService deviceService;

    public LiveStreamService(StringRedisTemplate stringRedisTemplate, DeviceService deviceService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.deviceService = deviceService;
    }

    public LiveFeedResponse getActiveFeeds(String serialNumber, Long deviceId) {
        deviceService.validateUserAccessToDevice(serialNumber);

        String pattern = "device:status:" + deviceId + ":*";
        // Use stringRedisTemplate to avoid Jackson JSON parsing
        Set<String> keys = stringRedisTemplate.keys(pattern);
        
        Map<String, String> feeds = new HashMap<>();
        if (keys != null) {
            for (String key : keys) {
                String cameraName = key.substring(key.lastIndexOf(":") + 1);
                
                // stringRedisTemplate.opsForValue().get(key) returns a raw String
                String url = stringRedisTemplate.opsForValue().get(key);
                feeds.put(cameraName, url);
            }
        }
        return new LiveFeedResponse(feeds);
    }
}
