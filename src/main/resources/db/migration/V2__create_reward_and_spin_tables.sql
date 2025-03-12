-- Reward and spin history tables

-- Reward table
CREATE TABLE IF NOT EXISTS reward (
    id BIGSERIAL PRIMARY KEY,
    event_location_id BIGINT NOT NULL REFERENCES event_location(id),
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
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255)
);

-- Golden Hour table
CREATE TABLE IF NOT EXISTS golden_hour (
    id BIGSERIAL PRIMARY KEY,
    event_location_id BIGINT NOT NULL REFERENCES event_location(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    win_probability DOUBLE PRECISION DEFAULT 0.0,
    status INTEGER DEFAULT 1,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255)
);

-- Participant Event join table
CREATE TABLE IF NOT EXISTS participant_event (
    id BIGSERIAL PRIMARY KEY,
    participant_id BIGINT NOT NULL REFERENCES participant(id),
    event_location_id BIGINT NOT NULL REFERENCES event_location(id),
    total_spins INTEGER DEFAULT 0,
    remaining_spins INTEGER DEFAULT 0,
    initial_spins INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    UNIQUE(participant_id, event_location_id)
);

-- Spin History table
CREATE TABLE IF NOT EXISTS spin_history (
    id BIGSERIAL PRIMARY KEY,
    participant_id BIGINT NOT NULL REFERENCES participant(id),
    event_location_id BIGINT NOT NULL REFERENCES event_location(id),
    reward_id BIGINT REFERENCES reward(id),
    golden_hour_id BIGINT REFERENCES golden_hour(id),
    timestamp TIMESTAMP NOT NULL,
    win BOOLEAN DEFAULT FALSE,
    points_earned INTEGER DEFAULT 0,
    points_spent INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_reward_code ON reward(code);
CREATE INDEX IF NOT EXISTS idx_reward_event_location ON reward(event_location_id);
CREATE INDEX IF NOT EXISTS idx_reward_valid_dates ON reward(valid_from, valid_until);
CREATE INDEX IF NOT EXISTS idx_golden_hour_event_location ON golden_hour(event_location_id);
CREATE INDEX IF NOT EXISTS idx_golden_hour_times ON golden_hour(start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_participant_event_participant ON participant_event(participant_id);
CREATE INDEX IF NOT EXISTS idx_participant_event_location ON participant_event(event_location_id);
CREATE INDEX IF NOT EXISTS idx_spin_history_participant ON spin_history(participant_id);
CREATE INDEX IF NOT EXISTS idx_spin_history_event_location ON spin_history(event_location_id);
CREATE INDEX IF NOT EXISTS idx_spin_history_reward ON spin_history(reward_id);
CREATE INDEX IF NOT EXISTS idx_spin_history_golden_hour ON spin_history(golden_hour_id);
CREATE INDEX IF NOT EXISTS idx_spin_history_timestamp ON spin_history(timestamp);
