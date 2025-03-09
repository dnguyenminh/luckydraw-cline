-- Test database schema setup

-- Create database objects in correct order
CREATE TABLE IF NOT EXISTS regions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status INTEGER DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS provinces (
    id BIGSERIAL PRIMARY KEY,
    region_id BIGINT REFERENCES regions(id),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    status INTEGER DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    status INTEGER DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    status INTEGER DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS events (
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
    metadata TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS event_locations (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id),
    region_id BIGINT NOT NULL REFERENCES regions(id),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    initial_spins INTEGER DEFAULT 0,
    daily_spin_limit INTEGER DEFAULT 0,
    default_win_probability DOUBLE PRECISION DEFAULT 0.0,
    status INTEGER DEFAULT 1,
    metadata TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    UNIQUE(event_id, region_id)
);

CREATE TABLE IF NOT EXISTS participants (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255),
    account VARCHAR(50) NOT NULL UNIQUE,
    metadata TEXT,
    province_id BIGINT REFERENCES provinces(id),
    user_id BIGINT REFERENCES users(id),
    status INTEGER DEFAULT 1,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS participant_roles (
    participant_id BIGINT NOT NULL REFERENCES participants(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (participant_id, role_id)
);

CREATE TABLE IF NOT EXISTS rewards (
    id BIGSERIAL PRIMARY KEY,
    event_location_id BIGINT NOT NULL REFERENCES event_locations(id),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    points INTEGER DEFAULT 0,
    points_required INTEGER DEFAULT 0,
    total_quantity INTEGER DEFAULT 0,
    remaining_quantity INTEGER DEFAULT 0,
    daily_limit INTEGER DEFAULT 0,
    win_probability DOUBLE PRECISION DEFAULT 0.0,
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    status INTEGER DEFAULT 1,
    metadata TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS golden_hours (
    id BIGSERIAL PRIMARY KEY,
    event_location_id BIGINT NOT NULL REFERENCES event_locations(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    win_probability DOUBLE PRECISION DEFAULT 0.0,
    status INTEGER DEFAULT 1,
    metadata TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS participant_events (
    id BIGSERIAL PRIMARY KEY,
    participant_id BIGINT NOT NULL REFERENCES participants(id),
    event_location_id BIGINT NOT NULL REFERENCES event_locations(id),
    total_spins INTEGER DEFAULT 0,
    available_spins INTEGER DEFAULT 0,
    daily_spin_count INTEGER DEFAULT 0,
    total_wins INTEGER DEFAULT 0,
    total_points INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    metadata TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    UNIQUE(participant_id, event_location_id)
);

CREATE TABLE IF NOT EXISTS spin_histories (
    id BIGSERIAL PRIMARY KEY,
    participant_id BIGINT NOT NULL REFERENCES participants(id),
    event_location_id BIGINT NOT NULL REFERENCES event_locations(id),
    reward_id BIGINT REFERENCES rewards(id),
    golden_hour_id BIGINT REFERENCES golden_hours(id),
    timestamp TIMESTAMP NOT NULL,
    win BOOLEAN DEFAULT FALSE,
    points_earned INTEGER DEFAULT 0,
    points_spent INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    metadata TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_event_code ON events(code);
CREATE INDEX IF NOT EXISTS idx_event_dates ON events(start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_event_location_code ON event_locations(code);
CREATE INDEX IF NOT EXISTS idx_participant_code ON participants(code);
CREATE INDEX IF NOT EXISTS idx_reward_code ON rewards(code);
CREATE INDEX IF NOT EXISTS idx_spin_history_date ON spin_histories(timestamp);
