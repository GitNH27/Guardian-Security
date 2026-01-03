package com.GuardianSecurity.security_backend.service;

import com.GuardianSecurity.security_backend.model.ThreatRecord;
import com.GuardianSecurity.security_backend.dto.response.ThreatLogResponse;
import com.GuardianSecurity.security_backend.repository.ThreatRecordRepository;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ThreatLogService {

    private final ThreatRecordRepository threatRecordRepository;
    private final DeviceService deviceService;

    @Value("${azure.storage.checkpoint-connection-string}")
    private String storageConnectionString;

    public ThreatLogService(ThreatRecordRepository threatRecordRepository, DeviceService deviceService) {
        this.threatRecordRepository = threatRecordRepository;
        this.deviceService = deviceService;
    }

    public List<ThreatLogResponse> getThreatLogsForDevice(String serialNumber) {
        deviceService.validateUserAccessToDevice(serialNumber);

        List<ThreatRecord> records = threatRecordRepository.findByDevice_SerialNumber(serialNumber);
        
        return records.stream()
                .map(record -> new ThreatLogResponse(
                        record.getId(),
                        record.getThreatLevel(),
                        record.getObjectDetected(),
                        record.getCameraTopic(),
                        generateSasUrl(record.getPhotoUrl()), // 🔥 Generate temporary access
                        record.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    private String generateSasUrl(String blobUrl) {
        if (blobUrl == null || blobUrl.isEmpty()) return null;
        
        try {
            BlobClient blobClient = new BlobClientBuilder()
                    .connectionString(storageConnectionString)
                    .endpoint(blobUrl)
                    .buildClient();

            // Create a SAS token valid for 15 minutes
            BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(15);
            BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission);

            return blobUrl + "?" + blobClient.generateSas(values);
        } catch (Exception e) {
            return blobUrl; // Fallback to raw URL if signing fails
        }
    }
}