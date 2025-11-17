package com.GuardianSecurity.security_backend.repository;

import com.GuardianSecurity.security_backend.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access layer for Device entity
 * 
 * Required methods:
 * - findBySerialNumber(String serialNumber) - Find device by serial number
 * - findByDeviceSecret(String deviceSecret) - Find device by secret for WebSocket auth
 * - findByPairingPassword(String pairingPassword) - Find device by pairing password for claiming
 * - existsBySerialNumber(String serialNumber) - Check if serial number exists
 * - findByStatus(DeviceStatus status) - Find devices by status (ONLINE, OFFLINE, UNCLAIMED)
 * 
 * Spring Data JPA will automatically provide:
 * - save(Device device) - Save or update device
 * - findById(Long id) - Find device by ID
 * - findAll() - Get all devices
 * - deleteById(Long id) - Delete device by ID
 * - count() - Count total devices
 * 
 * Custom query methods for MVP:
 * - findOnlineDevices() - Get all online devices
 * - findUnclaimedDevices() - Get all unclaimed devices
 * - findByUserAccess(Long userId) - Get devices accessible by user (via DeviceAccess)
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findBySerialNumber(String serialNumber);
    Optional<Device> findByPairingPassword(String pairingPassword);
    boolean existsBySerialNumber(String serialNumber);
}
