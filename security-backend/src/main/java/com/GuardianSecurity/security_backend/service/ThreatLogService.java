package com.GuardianSecurity.security_backend.service;
import com.GuardianSecurity.security_backend.model.ThreatRecord;
import com.GuardianSecurity.security_backend.dto.response.ThreatLogResponse;
import com.GuardianSecurity.security_backend.repository.ThreatRecordRepository;
import com.GuardianSecurity.security_backend.repository.DeviceAccessRepository;
import com.GuardianSecurity.security_backend.service.DeviceService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ThreatLogService {

    private final ThreatRecordRepository threatRecordRepository;
    private final DeviceService deviceService; // To reuse your existing security checks

    public ThreatLogService(ThreatRecordRepository threatRecordRepository, DeviceService deviceService) {
        this.threatRecordRepository = threatRecordRepository;
        this.deviceService = deviceService;
    }

    public List<ThreatLogResponse> getThreatLogsForDevice(String serialNumber) {
        // Validate user access to the device
        deviceService.validateUserAccessToDevice(serialNumber);

        // Fetch threat records from the repository if access is valid
        List<ThreatRecord> records = threatRecordRepository.findByDevice_SerialNumber(serialNumber);
        return records.stream()
                .map(record -> new ThreatLogResponse(
                        record.getId(),
                        record.getThreatLevel(),
                        record.getObjectDetected(),
                        record.getCameraTopic(),
                        record.getCreatedAt() // Assuming you have a getCreatedAt() method
                ))
                .collect(Collectors.toList());
    }
}