-- Drop tables in correct order
DROP TABLE IF EXISTS spin_histories;
DROP TABLE IF EXISTS golden_hours;
DROP TABLE IF EXISTS rewards;
DROP TABLE IF EXISTS participant_events;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS event_provinces;
DROP TABLE IF EXISTS event_locations;
DROP TABLE IF EXISTS provinces;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS regions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;

-- Create regions table
CREATE TABLE regions (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    default_win_probability DOUBLE PRECISION
);

-- Create events table
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    initial_spins INTEGER DEFAULT 10,
    daily_spin_limit INTEGER DEFAULT 5,
    default_win_probability DOUBLE PRECISION DEFAULT 0.1,
    metadata TEXT,
    remaining_spins BIGINT
);

-- Create provinces table
CREATE TABLE provinces (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    default_win_probability DOUBLE PRECISION,
    region_id BIGINT REFERENCES regions(id)
);

-- Create event_locations table
CREATE TABLE event_locations (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    region_id BIGINT REFERENCES regions(id),
    event_id BIGINT REFERENCES events(id)
);

-- Create roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    name VARCHAR(50) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    priority INTEGER DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1
);

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(20),
    position VARCHAR(50),
    status INTEGER NOT NULL DEFAULT 1,
    credentials_expired BOOLEAN DEFAULT false,
    account_expired BOOLEAN DEFAULT false,
    locked_until TIMESTAMP
);

-- Create user_roles junction table
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id),
    role_id BIGINT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- Create golden_hours table
CREATE TABLE golden_hours (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    event_id BIGINT REFERENCES events(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    win_probability DOUBLE PRECISION NOT NULL
);

-- Create rewards table
CREATE TABLE rewards (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    total_quantity INTEGER NOT NULL,
    available_quantity INTEGER NOT NULL,
    win_probability DOUBLE PRECISION NOT NULL,
    event_id BIGINT REFERENCES events(id)
);

-- Create participant_events table
CREATE TABLE participant_events (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    participant_id BIGINT REFERENCES users(id),
    event_id BIGINT REFERENCES events(id),
    location_id BIGINT REFERENCES event_locations(id),
    remaining_spins INTEGER NOT NULL DEFAULT 0,
    total_spins INTEGER NOT NULL DEFAULT 0,
    today_spins INTEGER NOT NULL DEFAULT 0,
    last_spin_at TIMESTAMP,
    UNIQUE(participant_id, event_id)
);

-- Create spin_histories table
CREATE TABLE spin_histories (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    participant_event_id BIGINT REFERENCES participant_events(id),
    reward_id BIGINT REFERENCES rewards(id),
    spin_time TIMESTAMP NOT NULL,
    is_win BOOLEAN NOT NULL DEFAULT false,
    is_finalized BOOLEAN NOT NULL DEFAULT false
);

-- Create event_provinces junction table
CREATE TABLE event_provinces (
    event_id BIGINT REFERENCES events(id),
    province_id BIGINT REFERENCES provinces(id),
    PRIMARY KEY (event_id, province_id)
);
