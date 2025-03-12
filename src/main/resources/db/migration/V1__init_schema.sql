-- Initial schema creation

-- Region table
CREATE TABLE IF NOT EXISTS region (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status INTEGER DEFAULT 1,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255)
);

-- Event table
CREATE TABLE IF NOT EXISTS event (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    initial_spins INTEGER DEFAULT 0,
    daily_spin_limit INTEGER DEFAULT 0,
    default_win_probability DOUBLE PRECISION DEFAULT 0.0,
    status INTEGER DEFAULT 1,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255)
);

-- Event location table
CREATE TABLE IF NOT EXISTS event_location (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES event(id),
    region_id BIGINT NOT NULL REFERENCES region(id),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    initial_spins INTEGER DEFAULT 0,
    daily_spin_limit INTEGER DEFAULT 0,
    default_win_probability DOUBLE PRECISION DEFAULT 0.0,
    status INTEGER DEFAULT 1,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    UNIQUE(event_id, region_id)
);

-- Participant table
CREATE TABLE IF NOT EXISTS participant (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255),
    status INTEGER DEFAULT 1,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_event_code ON event(code);
CREATE INDEX IF NOT EXISTS idx_event_status ON event(status);
CREATE INDEX IF NOT EXISTS idx_event_dates ON event(start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_event_location_code ON event_location(code);
CREATE INDEX IF NOT EXISTS idx_participant_code ON participant(code);
CREATE INDEX IF NOT EXISTS idx_participant_phone ON participant(phone);
CREATE INDEX IF NOT EXISTS idx_participant_email ON participant(email);
