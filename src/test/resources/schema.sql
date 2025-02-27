DROP TABLE IF EXISTS spin_histories;
DROP TABLE IF EXISTS golden_hours;
DROP TABLE IF EXISTS rewards;
DROP TABLE IF EXISTS participants;
DROP TABLE IF EXISTS event_locations;
DROP TABLE IF EXISTS events;

CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    total_spins INT,
    remaining_spins INT,
    is_active BOOLEAN DEFAULT true,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    modified_by VARCHAR(50)
);

CREATE TABLE event_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    province VARCHAR(255),
    total_spins INT,
    remaining_spins INT,
    daily_spin_limit INT,
    spins_remaining BIGINT,
    is_active BOOLEAN DEFAULT true,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    modified_by VARCHAR(50),
    FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE TABLE participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    event_location_id BIGINT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255),
    total_spins INT,
    remaining_spins INT,
    daily_spin_limit INT,
    last_spin_time TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    modified_by VARCHAR(50),
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (event_location_id) REFERENCES event_locations(id)
);

CREATE TABLE rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(255),
    total_quantity INT,
    remaining_quantity INT,
    daily_limit INT,
    daily_usage_count INT DEFAULT 0,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    probability DOUBLE,
    is_active BOOLEAN DEFAULT true,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    modified_by VARCHAR(50),
    FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE TABLE golden_hours (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    multiplier DOUBLE DEFAULT 1.0,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    modified_by VARCHAR(50),
    FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE TABLE spin_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    reward_id BIGINT,
    spin_time TIMESTAMP NOT NULL,
    remaining_spins BIGINT,
    won BOOLEAN DEFAULT false,
    location VARCHAR(50),
    result VARCHAR(255),
    is_golden_hour BOOLEAN DEFAULT false,
    current_multiplier DOUBLE DEFAULT 1.0,
    claim_time TIMESTAMP,
    is_claimed BOOLEAN DEFAULT false,
    claim_location VARCHAR(50),
    package_number INTEGER,
    claim_code VARCHAR(50),
    claim_notes TEXT,
    is_active BOOLEAN DEFAULT true,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    modified_by VARCHAR(50),
    FOREIGN KEY (event_id) REFERENCES events(id),
    FOREIGN KEY (participant_id) REFERENCES participants(id),
    FOREIGN KEY (reward_id) REFERENCES rewards(id)
);

-- Indexes for better query performance
CREATE INDEX idx_spin_histories_participant_id ON spin_histories(participant_id);
CREATE INDEX idx_spin_histories_event_id ON spin_histories(event_id);
CREATE INDEX idx_spin_histories_reward_id ON spin_histories(reward_id);
CREATE INDEX idx_spin_histories_spin_time ON spin_histories(spin_time);
CREATE INDEX idx_spin_histories_is_claimed ON spin_histories(is_claimed);
CREATE INDEX idx_spin_histories_claim_time ON spin_histories(claim_time);
CREATE INDEX idx_spin_histories_package_number ON spin_histories(package_number);
CREATE INDEX idx_spin_histories_won_claimed ON spin_histories(won, is_claimed);