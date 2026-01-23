package com.GuardianSecurity.security_backend.service;

import com.GuardianSecurity.security_backend.model.ThreatRecord;
import com.GuardianSecurity.security_backend.dto.response.ThreatLogResponse;
import com.GuardianSecurity.security_backend.repository.ThreatRecordRepository;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
//Import predicate
import jakarta.persistence.criteria.Predicate;

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

    // Get threat logs for a specific device (Now includes filters by - Camera Topic, Threat Level, Classification, Date)
    
public List<ThreatLogResponse> getThreatLogsFilter(
    String serialNumber, 
    String cameraTopic, 
    String threatLevel, 
    String objectDetected, 
    LocalDateTime startDate, 
    LocalDateTime endDate
) {
    deviceService.validateUserAccessToDevice(serialNumber);

    Specification<ThreatRecord> spec = (root, query, cb) -> {
        List<Predicate> predicates = new ArrayList<>();

        // 1. Mandatory Serial Number Filter
        predicates.add(cb.equal(root.get("device").get("serialNumber"), serialNumber));

        // 2. Camera Topic: Matches 'threat_topic' from your Python script (e.g., 'car/ml/front')
        if (cameraTopic != null && !cameraTopic.isEmpty()) {
            // Using 'like' allows the user to search 'front' to match 'car/ml/front'
            predicates.add(cb.like(cb.lower(root.get("cameraTopic")), "%" + cameraTopic.toLowerCase() + "%"));
        }

        // 3. Threat Level: Matches 'ml_data.level' (HIGH, MEDIUM, LOW)
        if (threatLevel != null && !threatLevel.isEmpty()) {
            // Your script sends "MEDIUM", ensure your DB/Backend logic maps MED to MEDIUM if needed
            String levelQuery = threatLevel.equals("MED") ? "MEDIUM" : threatLevel.toUpperCase();
            predicates.add(cb.equal(root.get("threatLevel"), levelQuery));
        }

        // 4. Classification: Matches 'ml_data.object' (e.g., 'Person')
        if (objectDetected != null && !objectDetected.isEmpty()) {
            predicates.add(cb.equal(root.get("objectDetected"), objectDetected));
        }

        // 5. Date Filtering: Exact minute-by-minute matching
        if (startDate != null && endDate != null) {
            predicates.add(cb.between(root.get("createdAt"), startDate, endDate));
        } else if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
        } else if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
        }

        query.orderBy(cb.desc(root.get("createdAt")));
        return cb.and(predicates.toArray(new Predicate[0]));
    };

    return threatRecordRepository.findAll(spec).stream()
            .map(record -> new ThreatLogResponse(
                record.getId(),
                record.getThreatLevel(),
                record.getObjectDetected(),
                record.getCameraTopic(),
                generateSasUrl(record.getPhotoUrl()),
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

    public String generateSasUrl(String blobUrl, boolean forceDownload) {
        if (blobUrl == null || blobUrl.isEmpty()) return null;
        
        try {
            BlobClient blobClient = new BlobClientBuilder()
                    .connectionString(storageConnectionString)
                    .endpoint(blobUrl)
                    .buildClient();

            BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(15);
            BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission);

            // This is the magic part that won't affect your frontend unless you ask for it
            if (forceDownload) {
                values.setContentDisposition("attachment; filename=\"security_log.jpg\"");
            }

            return blobUrl + "?" + blobClient.generateSas(values);
        } catch (Exception e) {
            return blobUrl;
        }
    }
}