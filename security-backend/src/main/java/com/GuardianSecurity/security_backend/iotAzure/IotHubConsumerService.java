package com.GuardianSecurity.security_backend.iotAzure;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class IotHubConsumerService {

    private static final Logger log = LoggerFactory.getLogger(IotHubConsumerService.class);

    private final ThreatDataProcessor threatDataProcessor;
    private EventProcessorClient eventProcessorClient;

    @Value("${azure.iothub.ehub-connection-string}")
    private String eventHubConnectionString;

    @Value("${azure.iothub.consumer-group}")
    private String consumerGroup;

    @Value("${azure.storage.checkpoint-connection-string}")
    private String storageConnectionString;

    @Value("${azure.storage.checkpoint-container}")
    private String containerName;

    // Dependency injection constructor
    public IotHubConsumerService(ThreatDataProcessor threatDataProcessor) {
        this.threatDataProcessor = threatDataProcessor;
    }

    /**
     * Called automatically when Spring Boot starts.
     * This bootstraps the IoT Hub → Backend stream.
     */
    @PostConstruct
    public void startProcessor() {
        log.info("Starting IoT Hub Event Processor...");

        BlobContainerAsyncClient blobContainerClient =
                        new BlobContainerClientBuilder()
                                .connectionString(storageConnectionString)
                                .containerName(containerName)
                                .buildAsyncClient();

        BlobCheckpointStore checkpointStore =
                new BlobCheckpointStore(blobContainerClient);

        this.eventProcessorClient =
                new EventProcessorClientBuilder()
                        .connectionString(eventHubConnectionString)
                        .consumerGroup(consumerGroup)
                        .processEvent(this::processEvent)
                        .processError(this::processError)
                        .checkpointStore(checkpointStore)
                        .buildEventProcessorClient();

        eventProcessorClient.start();
        log.info("IoT Hub Event Processor started successfully.");
    }

    /**
     * Called automatically when Spring Boot shuts down.
     * Ensures clean shutdown and checkpoint consistency.
     */
    @PreDestroy
    public void stopProcessor() {
        if (eventProcessorClient != null) {
            log.info("Stopping IoT Hub Event Processor...");
            eventProcessorClient.stop();
        }
    }

    /**
     * Core message handler.
     * Runs EVERY TIME a device sends telemetry to IoT Hub.
     */
    private void processEvent(EventContext context) {
        String payload =
                new String(context.getEventData()
                        .getBody(),
                        StandardCharsets.UTF_8);

        log.info("Received IoT message | Partition: {} | Offset: {}",
                context.getPartitionContext().getPartitionId(),
                context.getEventData().getOffsetString());

        log.info("Payload: {}", payload);

        try {
            // Call the service that parses the JSON, saves to Postgres, and pushes to Redis.
            threatDataProcessor.handleMessage(payload);
            log.info("Message processed successfully. Updating checkpoint.");
            context.updateCheckpoint(); 

        } catch (Exception e) {
            log.error("Failed to process message payload. Skipping checkpoint to force retry.", e);
        }
    }

    /**
     * Handles non-fatal errors while reading the stream.
     */
    private void processError(ErrorContext errorContext) {
        log.error(
                "Error in partition {}: {}",
                errorContext.getPartitionContext().getPartitionId(),
                errorContext.getThrowable().getMessage(),
                errorContext.getThrowable()
        );
    }
}