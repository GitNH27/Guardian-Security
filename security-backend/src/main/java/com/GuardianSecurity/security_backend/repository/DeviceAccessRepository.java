package com.GuardianSecurity.security_backend.repository;

import com.GuardianSecurity.security_backend.model.DeviceAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeviceAccessRepository extends JpaRepository<DeviceAccess, Long> {
    Optional<DeviceAccess> findByUserIdAndDeviceSerialNumber(Long userId, String serialNumber);
    Optional<DeviceAccess> findByUserId(Long userId);
    Optional<DeviceAccess> findByDeviceSerialNumber(String serialNumber);
}
