package com.GuardianSecurity.security_backend.iotAzure;

import com.GuardianSecurity.security_backend.dto.internal.MlDataPayload;
import com.GuardianSecurity.security_backend.model.Device;
import com.GuardianSecurity.security_backend.model.ThreatRecord;
import com.GuardianSecurity.security_backend.repository.DeviceRepository;
import com.GuardianSecurity.security_backend.repository.ThreatRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.GuardianSecurity.security_backend.service.FcmNotificationService;
import com.GuardianSecurity.security_backend.service.ThreatLogService;
import com.GuardianSecurity.security_backend.model.User;

import java.io.IOException;
import java.time.LocalDateTime; // Use LocalDateTime consistently
import java.util.Map;
import java.util.List;
import java.time.Duration;

// Notes on Code and assumptions:
// - The IoT device sends a JSON payload with a timestamp in ISO-8601 format.
// - The ThreatRecord entity uses LocalDateTime for the recorded_at field.
// - The RedisTemplate is injected and used to publish alerts to a specific channel.
// - The RedisTemplate is used to publish alerts to a specific channel.

@Service
public class ThreatDataProcessor {

    private static final Logger log = LoggerFactory.getLogger(ThreatDataProcessor.class);
    private final ObjectMapper objectMapper;
    private final ThreatRecordRepository recordRepository;
    private final DeviceRepository deviceRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ThreatLogService threatLogService;
    private final FcmNotificationService fcmNotificationService;

    // Define the Redis channel name for alerts
    private static final String REDIS_ALERT_CHANNEL = "threat-alerts"; 

    public ThreatDataProcessor(ObjectMapper objectMapper, 
                               ThreatRecordRepository recordRepository, 
                               DeviceRepository deviceRepository,
                               RedisTemplate<String, Object> redisTemplate,
                               ThreatLogService threatLogService,
                               FcmNotificationService fcmNotificationService) {
        this.objectMapper = objectMapper;
        this.recordRepository = recordRepository;
        this.deviceRepository = deviceRepository;
        this.redisTemplate = redisTemplate;
        this.threatLogService = threatLogService;
        this.fcmNotificationService = fcmNotificationService;
    }

    // Heartbeat monitoring function
    private void handleHeartbeat(Map<String, Object> dataMap) {
        Object deviceId = dataMap.get("deviceId");
        if (deviceId == null) {
            log.warn("Received heartbeat without a deviceId. Ignoring.");
            return;
        }

        String heartbeatKey = "device:heartbeat:" + deviceId.toString();
        
        // Update the heartbeat in Redis every 45 seconds
        redisTemplate.opsForValue().set(heartbeatKey, "ONLINE", java.time.Duration.ofSeconds(45));
        
        log.info("Heartbeat processed for Device: {}. Status: ONLINE", deviceId);
    }

    /**
     * Entry point for processing messages received from the Azure IoT Hub stream.
     */
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

            // 1. Permanent Persistence
            recordRepository.save(record);

            // 2. Logic Differentiation: INTRUDER vs HIGH THREAT
            if ("INTRUDER".equals(messageType)) {
                triggerImminentThreat(record);
            }
            
            // 3. Notification & Live Update Logic
            handleNotifications(record, liveUrl, messageType);

        } catch (IOException e) {
            log.error("Failed to parse IoT payload", e);
        }
    }

    private void triggerImminentThreat(ThreatRecord record) {
        String dangerKey = "device:status:danger:" + record.getRawDeviceId();
        // Set Redis 'DANGER' flag for 5 minutes for Dashboard UI
        redisTemplate.opsForValue().set(dangerKey, "DANGER", Duration.ofMinutes(5));
        log.error("IMMINENT THREAT: Device {} is in DANGER mode", record.getRawDeviceId());
    }

    private void handleNotifications(ThreatRecord record, String liveUrl, String type) {
        // 1. Get EVERYONE linked to this car instead of just one owner
        List<User> usersToNotify = threatLogService.getAllUsersWithAccess(record.getDevice().getId());

        // Prepare Push Notification Data
        boolean isIntruder = "INTRUDER".equals(type);
        String title = isIntruder ? "IMMINENT THREAT" : "High Threat Detected";
        String body = record.getObjectDetected() + " detected near your car!";
        
        // 2. Loop through every user and trigger their individual FCM Push
        if (usersToNotify != null && !usersToNotify.isEmpty() && ("HIGH".equals(record.getThreatLevel()) || isIntruder)) {
            for (User user : usersToNotify) {
                try {
                    fcmNotificationService.sendThreatNotification(user, title, body, isIntruder);
                } catch (Exception e) {
                    log.error("Failed to send notification to user: {}", user.getEmail(), e);
                }
            }
        }

        // 3. Cache live URL in Redis for Dashboard (Same as before)
        if (liveUrl != null) {
            String statusKey = "device:status:" + record.getRawDeviceId() + ":" + record.getCameraTopic().replace("/", ":");
            redisTemplate.opsForValue().set(statusKey, liveUrl, Duration.ofMinutes(3));
        }

        // 4. Broadcast to WebSockets (Same as before)
        record.setLiveStreamUrl(liveUrl);
        redisTemplate.convertAndSend(REDIS_ALERT_CHANNEL, record);
    }
    private String extractLiveUrl(Map<String, Object> dataMap) {
        if (dataMap.containsKey("ml_data")) {
            Map<String, Object> ml = (Map<String, Object>) dataMap.get("ml_data");
            return (String) ml.get("liveStreamUrl");
        }
        return null;
    }

    // This method maps the JSON data to a ThreatRecord object
    private ThreatRecord mapToThreatRecord(Map<String, Object> dataMap) {
        ThreatRecord record = new ThreatRecord();

        // --- 1. DEVICE ID EXTRACTION AND LOOKUP (FIXED) ---
        Object deviceIdObject = dataMap.get("deviceId");
        
        if (deviceIdObject == null) {
            throw new IllegalArgumentException("IoT payload is missing required 'deviceId'.");
        }

        // We now catch only IllegalArgumentException, which covers NumberFormatException
        try {
            Long deviceId;
            if (deviceIdObject instanceof Integer) {
                deviceId = ((Integer) deviceIdObject).longValue();
            } else if (deviceIdObject instanceof Long) {
                deviceId = (Long) deviceIdObject;
            } else if (deviceIdObject instanceof String) {
                // Long.parseLong() throws NumberFormatException (subclass of IllegalArgumentException)
                deviceId = Long.parseLong((String) deviceIdObject);
            } else {
                throw new IllegalArgumentException("Device ID type is unsupported: " + deviceIdObject.getClass().getSimpleName());
            }

            // Set the rawDeviceId field (String representation of the Device ID)
            record.setRawDeviceId(deviceId); 

            // CRITICAL STEP: LOOK UP THE DEVICE ENTITY
            Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device with ID " + deviceId + " not found."));

            // Set the Device entity, satisfying the @ManyToOne relationship
            record.setDevice(device);
            
        } catch (IllegalArgumentException e) { // <-- Simplified catch block
            // Catch parsing issues or lookup failure and log the warning
            log.warn("Threat Record mapping failed due to device issue: {}", e.getMessage());
            // Re-throw to stop transaction and prevent checkpointing
            throw new RuntimeException("Device validation failed during mapping.", e);
        }
        // --- END DEVICE LOOKUP ---

        // --- 2. Map other simple fields (Safe to keep as is, but ensure consistency) ---
        record.setCameraTopic((String) dataMap.get("threat_topic"));
        
        // ... (Timestamp and MlData mapping logic below is fine) ...
        
        String recordedAtString = (String) dataMap.get("recorded_at");
        // NOTE: You may need a specific DateTimeFormatter if the string isn't standard ISO.
        if (recordedAtString != null) {
            record.setCreatedAt(LocalDateTime.parse(recordedAtString));
        } else {
            record.setCreatedAt(LocalDateTime.now());
        }

        // --- 3. SAFE MAPPING OF NESTED OBJECT ---
        Object rawMlData = dataMap.get("ml_data"); 
        if (rawMlData != null) {
            try {
                MlDataPayload mlData = objectMapper.convertValue(rawMlData, MlDataPayload.class);
                record.setThreatLevel(mlData.getLevel());
                
                // --- LOGIC CHANGE START ---
                if (mlData.getObjects() != null && !mlData.getObjects().isEmpty()) {
                    // Join the list ["Person", "Gun"] into "Person, Gun"
                    String joined = String.join(", ", mlData.getObjects());
                    record.setObjectDetected(joined);
                } else {
                    record.setObjectDetected("None");
                }
                // --- LOGIC CHANGE END ---

                // Note: Script 1 uses 'image_file' in the JSON
                record.setPhotoUrl(mlData.getUrl());
                String filename = mlData.getUrl();
                if (filename != null) {
                    String serial = record.getDevice().getSerialNumber(); // adjust getter if needed
                    record.setPhotoUrl(
                        "https://checkgroup44.blob.core.windows.net/threatimages/" + serial + "/" + filename
                    );
                }
                
            } catch (IllegalArgumentException e) {
                log.error("Failed to map inner ML data payload.", e);
                throw new RuntimeException("Malformed ML data structure received.", e);
            }
        }

        return record;
    }
}