package com.GuardianSecurity.security_backend.service;
import com.GuardianSecurity.security_backend.dto.response.LiveFeedResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class LiveStreamService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final DeviceService deviceService;

    public LiveStreamService(RedisTemplate<String, Object> redisTemplate, DeviceService deviceService) {
        this.redisTemplate = redisTemplate;
        this.deviceService = deviceService;
    }

    public LiveFeedResponse getActiveFeeds(String serialNumber, Long deviceId) {
        // SECURITY: Reuse your existing logic to make sure the user owns this device
        deviceService.validateUserAccessToDevice(serialNumber);

        // QUERY REDIS: Find all keys matching "device:status:1:*"
        String pattern = "device:status:" + deviceId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        
        Map<String, String> feeds = new HashMap<>();
        if (keys != null) {
            for (String key : keys) {
                // Extract camera name (e.g., front) from the Redis key
                String cameraName = key.substring(key.lastIndexOf(":") + 1);
                String url = (String) redisTemplate.opsForValue().get(key);
                feeds.put(cameraName, url);
            }
        }
        return new LiveFeedResponse(feeds);
    }
}
