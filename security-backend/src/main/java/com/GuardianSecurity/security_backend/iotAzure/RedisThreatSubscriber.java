package com.GuardianSecurity.security_backend.iotAzure;

import com.GuardianSecurity.security_backend.model.ThreatRecord;
import com.fasterxml.jackson.databind.ObjectMapper; // ADD THIS
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisThreatSubscriber implements MessageListener {
    private static final Logger log = LoggerFactory.getLogger(RedisThreatSubscriber.class);
    private final ObjectMapper objectMapper; // Use ObjectMapper directly
    private final SimpMessagingTemplate messagingTemplate;

    public RedisThreatSubscriber(ObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // Manually convert the Redis message body to our ThreatRecord class
            // This bypasses the ClassLoader / LinkedHashMap casting issue
            ThreatRecord threat = objectMapper.readValue(message.getBody(), ThreatRecord.class);

            if (threat != null) {
                // Send the threat alert to WebSocket subscribers (send messages to a specific device topic instead of a global one. This ensures data privacy.)
                messagingTemplate.convertAndSend(
                    "/topic/threats/" + threat.getRawDeviceId(), threat);

                // Log the alert for monitoring
                log.warn("Real-time threat processed!");
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