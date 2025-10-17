package com.GuardianSecurity.security_backend.repository;

import com.GuardianSecurity.security_backend.model.DeviceAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeviceAccessRepository extends JpaRepository<DeviceAccess, Long> {
    Optional<DeviceAccess> findByUserIdAndDeviceId(Long userId, Long deviceId);
    Optional<DeviceAccess> findByUserId(Long userId);
    Optional<DeviceAccess> findByDeviceId(Long deviceId);
}
