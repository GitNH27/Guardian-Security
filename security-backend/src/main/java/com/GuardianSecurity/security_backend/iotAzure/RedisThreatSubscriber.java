package com.GuardianSecurity.security_backend.iotAzure;

import com.GuardianSecurity.security_backend.model.ThreatRecord;
import com.fasterxml.jackson.databind.ObjectMapper; // ADD THIS
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
public class RedisThreatSubscriber implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(RedisThreatSubscriber.class);
    private final ObjectMapper objectMapper; // Use ObjectMapper directly

    public RedisThreatSubscriber(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // Manually convert the Redis message body to our ThreatRecord class
            // This bypasses the ClassLoader / LinkedHashMap casting issue
            ThreatRecord threat = objectMapper.readValue(message.getBody(), ThreatRecord.class);

            if (threat != null) {
                log.warn("🚨 [AUTOMATED ALERT] Real-time threat processed!");
                log.warn("   Device ID: {} | Level: {} | Object: {}", 
                         threat.getRawDeviceId(), 
                         threat.getThreatLevel(), 
                         threat.getObjectDetected());
            }
        } catch (Exception e) {
            log.error("Error in Redis Subscriber deserialization: ", e);
        }
    }
}