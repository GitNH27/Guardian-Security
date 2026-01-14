package com.GuardianSecurity.security_backend.controller;

import com.GuardianSecurity.security_backend.dto.response.ThreatLogResponse;
import com.GuardianSecurity.security_backend.service.ThreatLogService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class ThreatLogController {

    private final ThreatLogService threatLogService;

    public ThreatLogController(ThreatLogService threatLogService) {
        this.threatLogService = threatLogService;
    }

    // Endpoint to get all threat logs for a specific device
    // Endpoint to get all threat logs for a specific device via its serial number
    @GetMapping("/threats")
    public ResponseEntity<?> getDeviceLogs(
        @RequestParam String serialNumber,
        @RequestParam(required = false) String cameraTopic,
        @RequestParam(required = false) String threatLevel,
        @RequestParam(required = false) String objectDetected,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {


        // Serial Number is still mandatory for security/context
        if (serialNumber == null || serialNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: serialNumber is required.");
        }

        // Call the new filtered service method
        List<ThreatLogResponse> logs = threatLogService.getThreatLogsFilter(
            serialNumber, 
            cameraTopic, 
            threatLevel, 
            objectDetected, 
            start, 
            end
        );

        return ResponseEntity.status(HttpStatus.OK).body(logs);
    }
}