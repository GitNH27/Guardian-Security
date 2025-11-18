package com.GuardianSecurity.security_backend.repository;

import com.GuardianSecurity.security_backend.model.DeviceAccessPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeviceAccessPermissionRepository extends JpaRepository<DeviceAccessPermission, Long> {

    // 1. Find by Requester's ID (instead of findByUserId)
    Optional<DeviceAccessPermission> findByRequester_Id(Long requesterId);

    // 2. Find by Device Serial Number (instead of findByDeviceSerialNumber)
    Optional<DeviceAccessPermission> findByDevice_SerialNumber(String serialNumber);

    // 3. Find by BOTH (The method that previously failed)
    Optional<DeviceAccessPermission> findByRequester_IdAndDevice_SerialNumber(Long requesterId, String serialNumber);
}
