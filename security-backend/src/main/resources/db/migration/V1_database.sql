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
    pairing_password VARCHAR(12),
    status VARCHAR(20) NOT NULL DEFAULT 'UNCLAIMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_device_status CHECK (status IN ('UNCLAIMED', 'CLAIMED'))
);
-- Create device_access table linking users to devices with roles
CREATE TABLE device_access (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_serial_number BIGINT NOT NULL REFERENCES devices(serial_number) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Prevent duplicate entries
    CONSTRAINT unique_user_device UNIQUE (user_id, device_id),

    -- Allowed roles
    CONSTRAINT check_access_role CHECK (role IN ('OWNER', 'MEMBER'))
);

-- Table for storing user requests to join a device
CREATE TABLE device_access_requests (
    id BIGSERIAL PRIMARY KEY,

    requester_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- RECOMMENDED FIX: Reference the PK devices.id
    device_id BIGINT NOT NULL REFERENCES devices(id) ON DELETE CASCADE, 
    
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraint uses device_id (BIGINT)
    CONSTRAINT unique_request UNIQUE (requester_id, device_id), 

    CONSTRAINT check_request_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- Create threat_records table for storing telemetry alerts from IoT devices
CREATE TABLE threat_records (
    id BIGSERIAL PRIMARY KEY,
    
    -- Device-related fields
    device_id BIGINT NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    camera_topic VARCHAR(255), -- e.g., 'car/ml/camera/front'
    
    -- ML Data
    object_detected VARCHAR(255), -- e.g., 'Person', 'Animal'
    threat_level VARCHAR(50) NOT NULL, -- e.g., 'LOW', 'HIGH', 'VERY_HIGH'
    photo_url VARCHAR(512), -- URL to the BLOB storage location of the photo

    -- Timestamps
    created_at TIMESTAMP NOT NULL, -- The time the event happened on the device
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- The time the backend saved the record
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO devices (id, serial_number, pairing_password, status) VALUES ('1', 'DEV-GAMMA-789-RPI', 'DEMOPASS789', 'UNCLAIMED');

-- Create table for multiple FCM tokens per user
CREATE TABLE user_fcm_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token TEXT NOT NULL UNIQUE,
    device_name VARCHAR(255), -- e.g., 'Pixel 7', 'iPhone 15'
    last_used TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_token_per_user UNIQUE (user_id, token)
);

-- Create indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_devices_serial_number ON devices(serial_number);
CREATE INDEX idx_devices_pairing_password ON devices(pairing_password);
CREATE INDEX idx_devices_status ON devices(status);
CREATE INDEX idx_device_access_user_id ON device_access(user_id);
CREATE INDEX idx_device_access_device_id ON device_access(device_id);
CREATE INDEX idx_device_access_role ON device_access(role);
CREATE INDEX idx_fcm_tokens_user_id ON user_fcm_tokens(user_id);
