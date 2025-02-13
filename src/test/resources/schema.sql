-- Drop tables if they exist
DROP TABLE IF EXISTS lucky_draw_results CASCADE;
DROP TABLE IF EXISTS spin_histories CASCADE;
DROP TABLE IF EXISTS golden_hours CASCADE;
DROP TABLE IF EXISTS rewards CASCADE;
DROP TABLE IF EXISTS participants CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255)
);

-- Create roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Create user_roles table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- Create events table
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    total_spins BIGINT NOT NULL DEFAULT 0,
    remaining_spins BIGINT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    CONSTRAINT chk_event_dates CHECK (end_date > start_date)
);

CREATE INDEX idx_event_active_dates ON events (is_active, start_date, end_date);

-- Create participants table
CREATE TABLE participants (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    customer_id VARCHAR(255),
    card_number VARCHAR(255),
    email VARCHAR(255),
    employee_id VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    province VARCHAR(255),
    department VARCHAR(255),
    position VARCHAR(255),
    spins_remaining BIGINT DEFAULT 0,
    daily_spin_limit BIGINT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT unq_participant_event_employee UNIQUE (event_id, employee_id)
);

CREATE INDEX idx_participant_event_active ON participants (event_id, is_active);
CREATE INDEX idx_participant_identifiers ON participants (customer_id, card_number, email);

-- Create rewards table
CREATE TABLE rewards (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    quantity INTEGER NOT NULL,
    remaining_quantity INTEGER NOT NULL,
    max_quantity_in_period INTEGER,
    probability DOUBLE PRECISION,
    applicable_provinces VARCHAR(1000),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT chk_reward_quantity CHECK (remaining_quantity >= 0 AND remaining_quantity <= quantity)
);

CREATE INDEX idx_reward_event_active ON rewards (event_id, is_active);

-- Create spin_histories table
CREATE TABLE spin_histories (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    reward_id BIGINT,
    spin_time TIMESTAMP NOT NULL,
    result VARCHAR(50),
    is_golden_hour BOOLEAN DEFAULT false,
    won BOOLEAN DEFAULT false,
    current_multiplier DECIMAL(5,2),
    remaining_spins BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES events (id),
    FOREIGN KEY (participant_id) REFERENCES participants (id),
    FOREIGN KEY (reward_id) REFERENCES rewards (id)
);

CREATE INDEX idx_spin_history_participant ON spin_histories (participant_id, spin_time);
CREATE INDEX idx_spin_history_event ON spin_histories (event_id, spin_time);

-- Create lucky_draw_results table
CREATE TABLE lucky_draw_results (
    id BIGSERIAL PRIMARY KEY,
    participant_id BIGINT NOT NULL,
    reward_id BIGINT NOT NULL,
    spin_history_id BIGINT NOT NULL UNIQUE,
    win_time TIMESTAMP NOT NULL,
    pack_number INTEGER,
    is_claimed BOOLEAN DEFAULT false,
    claimed_at TIMESTAMP,
    claimed_by VARCHAR(255),
    claim_notes TEXT,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (participant_id) REFERENCES participants (id),
    FOREIGN KEY (reward_id) REFERENCES rewards (id),
    FOREIGN KEY (spin_history_id) REFERENCES spin_histories (id)
);

CREATE INDEX idx_lucky_draw_reward_pack ON lucky_draw_results (reward_id, pack_number);
CREATE INDEX idx_lucky_draw_win_time ON lucky_draw_results (win_time);

-- Create golden_hours table
CREATE TABLE golden_hours (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    name VARCHAR(255),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    reward_id BIGINT,
    multiplier DOUBLE PRECISION DEFAULT 1.0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES events (id),
    FOREIGN KEY (reward_id) REFERENCES rewards (id)
);

CREATE INDEX idx_golden_hour_event_active ON golden_hours (event_id, is_active);

-- Insert default roles
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_USER');