-- Database migration for MVP schema (User, Device, DeviceAccess only)

-- Create users table for authentication and user management
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    firstName VARCHAR(255) NOT NULL,
    lastName VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create devices table for Raspberry Pi device management
CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    serial_number VARCHAR(255) UNIQUE NOT NULL,
    device_secret VARCHAR(255) UNIQUE NOT NULL,
    pairing_password VARCHAR(12),
    status VARCHAR(20) NOT NULL DEFAULT 'UNCLAIMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_device_status CHECK (status IN ('UNCLAIMED', 'ONLINE', 'OFFLINE'))
);

-- Create device_access table linking users to devices with roles
CREATE TABLE device_access (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_id BIGINT NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'OWNER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_access_role CHECK (role IN ('OWNER')),
    CONSTRAINT unique_user_device UNIQUE (user_id, device_id)
);

-- Create indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_devices_serial_number ON devices(serial_number);
CREATE INDEX idx_devices_device_secret ON devices(device_secret);
CREATE INDEX idx_devices_pairing_password ON devices(pairing_password);
CREATE INDEX idx_devices_status ON devices(status);
CREATE INDEX idx_device_access_user_id ON device_access(user_id);
CREATE INDEX idx_device_access_device_id ON device_access(device_id);
CREATE INDEX idx_device_access_role ON device_access(role);