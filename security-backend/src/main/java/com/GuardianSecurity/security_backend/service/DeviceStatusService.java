package com.GuardianSecurity.security_backend.service;

import java.time.LocalDateTime;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.GuardianSecurity.security_backend.dto.response.DeviceStatusResponse;

@Service
public class DeviceStatusService {
    private final RedisTemplate<String, Object> redisTemplate;

    public DeviceStatusService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public DeviceStatusResponse getDeviceSystemStatus(Long deviceId) {
        String dangerKey = "device:status:danger:" + deviceId;
        String heartbeatKey = "device:heartbeat:" + deviceId;

        // Priority 1: INTRUDER/DANGER
        if (redisTemplate.hasKey(dangerKey)) {
            return new DeviceStatusResponse(deviceId, "DANGER", "INTRUDER DETECTED!", LocalDateTime.now());
        }

        // Priority 2: HEARTBEAT/ACTIVE
        if (redisTemplate.hasKey(heartbeatKey)) {
            return new DeviceStatusResponse(deviceId, "ACTIVE", "Guardian Active", LocalDateTime.now());
        }

        // Priority 3: OFFLINE
        return new DeviceStatusResponse(deviceId, "OFFLINE", "System Offline", LocalDateTime.now());
    }
}
