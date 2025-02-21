-- Clean up existing tables
DROP TABLE IF EXISTS user_roles CASCADE;

DROP TABLE IF EXISTS roles CASCADE;

DROP TABLE IF EXISTS users CASCADE;

DROP TABLE IF EXISTS participants CASCADE;

DROP TABLE IF EXISTS events CASCADE;

DROP TABLE IF EXISTS event_locations CASCADE;

DROP TABLE IF EXISTS rewards CASCADE;

DROP TABLE IF EXISTS spin_histories CASCADE;

DROP TABLE IF EXISTS golden_hours CASCADE;

-- Create tables
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(20),
    status VARCHAR(20),
    enabled BOOLEAN DEFAULT true,
    created_by VARCHAR(50),
    last_modified_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_by VARCHAR(50),
    last_modified_by VARCHAR(50),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    total_spins BIGINT NOT NULL DEFAULT 0,
    remaining_spins BIGINT NOT NULL DEFAULT 0,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE event_locations (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT REFERENCES events(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    total_spins BIGINT NOT NULL DEFAULT 0,
    remaining_spins BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE participants (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT REFERENCES events(id) ON DELETE CASCADE,
    event_location_id BIGINT REFERENCES event_locations(id) ON DELETE
    SET
        NULL,
        user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
        name VARCHAR(100) NOT NULL,
        full_name VARCHAR(200),
        email VARCHAR(100),
        phone_number VARCHAR(20),
        province VARCHAR(100),
        customer_id VARCHAR(100),
        employee_id VARCHAR(100),
        card_number VARCHAR(100),
        spins_remaining INT NOT NULL DEFAULT 0,
        daily_spin_limit BIGINT,
        is_active BOOLEAN DEFAULT true,
        is_eligible_for_spin BOOLEAN DEFAULT true,
        version BIGINT NOT NULL DEFAULT 0,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rewards (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT REFERENCES events(id) ON DELETE CASCADE,
    event_region_id BIGINT,
    code VARCHAR(50),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    applicable_provinces TEXT [],
    quantity INTEGER NOT NULL DEFAULT 0,
    remaining_quantity INTEGER NOT NULL DEFAULT 0,
    probability DOUBLE PRECISION NOT NULL DEFAULT 0,
    max_quantity_in_period INTEGER,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE golden_hours (
    id BIGSERIAL PRIMARY KEY,
    reward_id BIGINT REFERENCES rewards(id) ON DELETE CASCADE,
    event_id BIGINT REFERENCES events(id) ON DELETE CASCADE,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    multiplier DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    is_active BOOLEAN DEFAULT true,
    name VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE spin_histories (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT REFERENCES events(id) ON DELETE CASCADE,
    participant_id BIGINT REFERENCES participants(id) ON DELETE CASCADE,
    reward_id BIGINT REFERENCES rewards(id) ON DELETE SET NULL,
    spin_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_golden_hour BOOLEAN DEFAULT false,
    won BOOLEAN DEFAULT false,
    result TEXT,
    remaining_spins BIGINT,
    current_multiplier DECIMAL(5,2) NOT NULL DEFAULT 1.0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);