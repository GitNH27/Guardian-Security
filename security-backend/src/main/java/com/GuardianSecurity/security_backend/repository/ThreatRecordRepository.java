package com.GuardianSecurity.security_backend.repository;

import com.GuardianSecurity.security_backend.model.ThreatRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface ThreatRecordRepository extends JpaRepository<ThreatRecord, Long>, JpaSpecificationExecutor<ThreatRecord> {
    // Find the most recent threat record for a device by its serial number
    List<ThreatRecord> findTopByDevice_SerialNumberOrderByCreatedAtDesc(String serialNumber);

    // Find all threat records for a device by its serial number
    List<ThreatRecord> findByDevice_SerialNumber(String serialNumber);

}