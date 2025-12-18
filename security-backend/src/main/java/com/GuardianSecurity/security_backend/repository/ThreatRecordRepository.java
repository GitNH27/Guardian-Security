package com.GuardianSecurity.security_backend.repository;

import com.GuardianSecurity.security_backend.model.ThreatRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ThreatRecordRepository extends JpaRepository<ThreatRecord, Long> {
}