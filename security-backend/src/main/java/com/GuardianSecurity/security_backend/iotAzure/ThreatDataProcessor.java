package com.GuardianSecurity.security_backend.iotAzure;

import com.GuardianSecurity.security_backend.dto.internal.MlDataPayload;
import com.GuardianSecurity.security_backend.model.Device;
import com.GuardianSecurity.security_backend.model.ThreatRecord;
import com.GuardianSecurity.security_backend.repository.DeviceRepository;
import com.GuardianSecurity.security_backend.repository.ThreatRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate; // <-- NEW IMPORT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime; // Use LocalDateTime consistently
import java.util.Map;
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
    private final RedisTemplate<String, Object> redisTemplate; // <-- NEW INJECTION

    // Define the Redis channel name for alerts
    private static final String REDIS_ALERT_CHANNEL = "threat-alerts"; 

    public ThreatDataProcessor(ObjectMapper objectMapper, 
                               ThreatRecordRepository recordRepository, 
                               DeviceRepository deviceRepository,
                               RedisTemplate<String, Object> redisTemplate) { // <-- NEW PARAMETER
        this.objectMapper = objectMapper;
        this.recordRepository = recordRepository;
        this.deviceRepository = deviceRepository;
        this.redisTemplate = redisTemplate; // <-- INJECTED
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
        ThreatRecord record;
        String liveUrl = null;
        try {
            Map<String, Object> dataMap = objectMapper.readValue(rawJsonPayload, Map.class);

            if("HEARTBEAT".equals(dataMap.get("type"))){
                handleHeartbeat(dataMap);
                return;
            }

            record = mapToThreatRecord(dataMap);

            if (dataMap.containsKey("ml_data")) {
                Map<String, Object> ml = (Map<String, Object>) dataMap.get("ml_data");
                liveUrl = (String) ml.get("liveStreamUrl");
            }

            // Save the record to the database
            recordRepository.save(record);
            log.info("Saved Threat Record ID {} for Device: {} | Level: {}", 
                    record.getId(), record.getRawDeviceId(), record.getThreatLevel());

            if ("INTRUDER".equals(dataMap.get("type"))) {
                String dangerKey = "device:status:danger:" + record.getRawDeviceId();
                // Flag the device as DANGER for 5 minutes in Redis
                redisTemplate.opsForValue().set(dangerKey, "DANGER", Duration.ofMinutes(5));
                log.error("INTRUDER ALERT TRIGGERED for Device: {}", record.getRawDeviceId());
            }
            
            // 4. Notification Logic
            handleNotifications(record, liveUrl);

        } catch (IOException e) {
            log.error("Failed to parse incoming JSON payload: {}", rawJsonPayload, e);
            throw new RuntimeException("Invalid JSON format from IoT stream.", e);
        }
    }

    /**
     * Determines what notifications or real-time actions are needed.
     */
    private void handleNotifications(ThreatRecord record, String liveUrl) {
        if ("HIGH".equals(record.getThreatLevel())) {

            // Camera (Ex: car/ml/front)
            String cameraString = record.getCameraTopic().replace("/", ":");
            String statusKey = "device:status:" + record.getRawDeviceId() + ":" + cameraString;
        
            if (liveUrl != null) {
                // This stores the specific live feed for the FRONT or BACK camera
                redisTemplate.opsForValue().set(statusKey, liveUrl, java.time.Duration.ofMinutes(3));
                log.info("Live status cached for Camera: {} (Key: {})", record.getCameraTopic(), statusKey);
            }
            // Broadcast to WebSocket
            record.setLiveStreamUrl(liveUrl);   
            redisTemplate.convertAndSend(REDIS_ALERT_CHANNEL, record);
        }
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
                record.setObjectDetected(mlData.getObject()); 
                record.setPhotoUrl(mlData.getUrl());
            } catch (IllegalArgumentException e) {
                log.error("Failed to map inner ML data payload.", e);
                throw new RuntimeException("Malformed ML data structure received.", e);
            }
        }

        return record;
    }
}