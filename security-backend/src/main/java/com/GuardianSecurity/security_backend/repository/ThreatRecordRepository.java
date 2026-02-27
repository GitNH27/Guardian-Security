package com.GuardianSecurity.security_backend.repository;

import com.GuardianSecurity.security_backend.model.ThreatRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface ThreatRecordRepository extends JpaRepository<ThreatRecord, Long>, JpaSpecificationExecutor<ThreatRecord> {
    // Find the most recent threat record for a device by its serial number
    List<ThreatRecord> findTopByDevice_SerialNumberOrderByCreatedAtDesc(String serialNumber);

    // Find all threat records for a device by its serial number
    List<ThreatRecord> findByDevice_SerialNumber(String serialNumber);

    // In ThreatRecordRepository.java
    @Query("SELECT t FROM ThreatRecord t WHERE t.device.serialNumber = :serial " +
        "AND (:level IS NULL OR t.threatLevel = :level) " +
        "AND (:topic IS NULL OR t.cameraTopic = :topic) " +
        "AND (:start IS NULL OR t.createdAt >= :start) " +
        "AND (:end IS NULL OR t.createdAt <= :end)")
    List<ThreatRecord> findFilteredLogs(
        String serial, String level, String topic, 
        LocalDateTime start, LocalDateTime end);

}