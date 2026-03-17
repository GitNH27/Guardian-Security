package com.GuardianSecurity.security_backend.iotAzure;

import com.GuardianSecurity.security_backend.dto.internal.MlDataPayload;
import com.GuardianSecurity.security_backend.model.Device;
import com.GuardianSecurity.security_backend.model.ThreatRecord;
import com.GuardianSecurity.security_backend.repository.DeviceRepository;
import com.GuardianSecurity.security_backend.repository.ThreatRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate; // Changed
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.GuardianSecurity.security_backend.service.FcmNotificationService;
import com.GuardianSecurity.security_backend.service.ThreatLogService;
import com.GuardianSecurity.security_backend.model.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.time.Duration;

@Service
public class ThreatDataProcessor {

    private static final Logger log = LoggerFactory.getLogger(ThreatDataProcessor.class);
    private final ObjectMapper objectMapper;
    private final ThreatRecordRepository recordRepository;
    private final DeviceRepository deviceRepository;
    private final StringRedisTemplate redisTemplate; // Unified Template
    private final ThreatLogService threatLogService;
    private final FcmNotificationService fcmNotificationService;

    private static final String REDIS_ALERT_CHANNEL = "threat-alerts"; 

    public ThreatDataProcessor(ObjectMapper objectMapper, 
                               ThreatRecordRepository recordRepository, 
                               DeviceRepository deviceRepository,
                               StringRedisTemplate redisTemplate, // Unified Template
                               ThreatLogService threatLogService,
                               FcmNotificationService fcmNotificationService) {
        this.objectMapper = objectMapper;
        this.recordRepository = recordRepository;
        this.deviceRepository = deviceRepository;
        this.redisTemplate = redisTemplate;
        this.threatLogService = threatLogService;
        this.fcmNotificationService = fcmNotificationService;
    }

    private void handleHeartbeat(Map<String, Object> dataMap) {
        Object deviceId = dataMap.get("deviceId");
        if (deviceId == null) {
            log.warn("Received heartbeat without a deviceId. Ignoring.");
            return;
        }

        String heartbeatKey = "device:heartbeat:" + deviceId.toString();
        
        // Clean String set
        redisTemplate.opsForValue().set(heartbeatKey, "ONLINE", Duration.ofSeconds(45));
        
        log.info("Heartbeat processed for Device: {}. Status: ONLINE", deviceId);
    }

    @Transactional
    public void handleMessage(String rawJsonPayload) {
        try {
            Map<String, Object> dataMap = objectMapper.readValue(rawJsonPayload, Map.class);
            String messageType = (String) dataMap.get("type");

            if ("HEARTBEAT".equals(messageType)) {
                handleHeartbeat(dataMap);
                return;
            }

            ThreatRecord record = mapToThreatRecord(dataMap);
            String liveUrl = extractLiveUrl(dataMap);

            recordRepository.save(record);

            if ("INTRUDER".equals(messageType)) {
                triggerImminentThreat(record);
            }
            
            handleNotifications(record, liveUrl, messageType);

        } catch (IOException e) {
            log.error("Failed to parse IoT payload", e);
        }
    }

    private void triggerImminentThreat(ThreatRecord record) {
        String dangerKey = "device:status:danger:" + record.getRawDeviceId();
        redisTemplate.opsForValue().set(dangerKey, "DANGER", Duration.ofMinutes(5));
        log.error("IMMINENT THREAT: Device {} is in DANGER mode", record.getRawDeviceId());
    }

    private void handleNotifications(ThreatRecord record, String liveUrl, String type) {
        List<User> usersToNotify = threatLogService.getAllUsersWithAccess(record.getDevice().getId());

        boolean isIntruder = "INTRUDER".equals(type);
        String threatLevel = record.getThreatLevel(); // e.g., "MEDIUM"
        
        // Updated condition to allow HIGH, MEDIUM, and INTRUDER
        boolean shouldAlert = "HIGH".equals(threatLevel) || "MEDIUM".equals(threatLevel) || isIntruder;

        if (usersToNotify != null && !usersToNotify.isEmpty() && shouldAlert) {
            String title = isIntruder ? "IMMINENT THREAT" : (threatLevel + " Threat Detected");
            String body = record.getObjectDetected() + " detected near your car!";
            
            for (User user : usersToNotify) {
                try {
                    fcmNotificationService.sendThreatNotification(user, title, body, isIntruder);
                } catch (Exception e) {
                    log.error("Failed to send notification to user: {}", user.getEmail(), e);
                }
            }
        }

        // Ensure the liveUrl is cached in Redis for React to find it
        if (liveUrl != null && shouldAlert) {
            String statusKey = "device:status:" + record.getRawDeviceId() + ":" + record.getCameraTopic().replace("/", ":");
            redisTemplate.opsForValue().set(statusKey, liveUrl, Duration.ofMinutes(3));
        }

        // Always send the Pub/Sub so the Activity Log updates in real-time
        record.setLiveStreamUrl(liveUrl);
        try {
            String jsonPayload = objectMapper.writeValueAsString(record);
            redisTemplate.convertAndSend(REDIS_ALERT_CHANNEL, jsonPayload);
        } catch (Exception e) {
            log.error("Failed to serialize threat record for Redis channel", e);
        }
    }

    private String extractLiveUrl(Map<String, Object> dataMap) {
        if (dataMap.containsKey("ml_data")) {
            Map<String, Object> ml = (Map<String, Object>) dataMap.get("ml_data");
            String url = (String) ml.get("liveFeed");
            log.info("Extracted liveUrl: {}", url);
            return url;
        }
        return null;
    }

    private ThreatRecord mapToThreatRecord(Map<String, Object> dataMap) {
        ThreatRecord record = new ThreatRecord();
        Object deviceIdObject = dataMap.get("deviceId");
        
        if (deviceIdObject == null) {
            throw new IllegalArgumentException("IoT payload is missing required 'deviceId'.");
        }

        try {
            Long deviceId;
            if (deviceIdObject instanceof Integer) {
                deviceId = ((Integer) deviceIdObject).longValue();
            } else if (deviceIdObject instanceof Long) {
                deviceId = (Long) deviceIdObject;
            } else if (deviceIdObject instanceof String) {
                deviceId = Long.parseLong((String) deviceIdObject);
            } else {
                throw new IllegalArgumentException("Device ID type is unsupported: " + deviceIdObject.getClass().getSimpleName());
            }

            record.setRawDeviceId(deviceId); 

            Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device with ID " + deviceId + " not found."));

            record.setDevice(device);
            
        } catch (IllegalArgumentException e) {
            log.warn("Threat Record mapping failed due to device issue: {}", e.getMessage());
            throw new RuntimeException("Device validation failed during mapping.", e);
        }

        record.setCameraTopic((String) dataMap.get("threat_topic"));
        
        String recordedAtString = (String) dataMap.get("recorded_at");
        if (recordedAtString != null) {
            record.setCreatedAt(LocalDateTime.parse(recordedAtString));
        } else {
            record.setCreatedAt(LocalDateTime.now());
        }

        Object rawMlData = dataMap.get("ml_data"); 
        if (rawMlData != null) {
            try {
                MlDataPayload mlData = objectMapper.convertValue(rawMlData, MlDataPayload.class);
                record.setThreatLevel(mlData.getLevel());
                
                if (mlData.getObjects() != null && !mlData.getObjects().isEmpty()) {
                    String joined = String.join(", ", mlData.getObjects());
                    record.setObjectDetected(joined);
                } else {
                    record.setObjectDetected("None");
                }
                record.setPhotoUrl(mlData.getUrl());
                
            } catch (IllegalArgumentException e) {
                log.error("Failed to map inner ML data payload.", e);
                throw new RuntimeException("Malformed ML data structure received.", e);
            }
        }
        return record;
    }
}