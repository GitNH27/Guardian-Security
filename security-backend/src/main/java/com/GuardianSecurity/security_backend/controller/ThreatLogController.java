package com.GuardianSecurity.security_backend.controller;

import com.GuardianSecurity.security_backend.dto.response.ThreatLogResponse;
import com.GuardianSecurity.security_backend.service.ThreatLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        @RequestParam(value = "serialNumber", required = false) String serialNumber
    ) {
        // 1. Log exactly what the controller sees
        System.out.println("DEBUG: Received serialNumber = [" + serialNumber + "]");

        if (serialNumber == null) {
            return ResponseEntity.badRequest().body("Error: serialNumber parameter was null at the Controller level");
        }

        List<ThreatLogResponse> logs = threatLogService.getThreatLogsForDevice(serialNumber);
        return ResponseEntity.ok(logs);
    }
}