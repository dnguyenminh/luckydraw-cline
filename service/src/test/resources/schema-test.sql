DROP TABLE IF EXISTS spin_histories CASCADE;
DROP TABLE IF EXISTS rewards CASCADE;
DROP TABLE IF EXISTS participant_events CASCADE;
DROP TABLE IF EXISTS event_locations CASCADE;
DROP TABLE IF EXISTS participants CASCADE;
DROP TABLE IF EXISTS events CASCADE;

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    status INTEGER DEFAULT 1,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    version INTEGER DEFAULT 0,
    metadata TEXT
);

CREATE TABLE event_locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    event_id BIGINT REFERENCES events(id),
    status INTEGER DEFAULT 1,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    version INTEGER DEFAULT 0,
    metadata TEXT
);

CREATE TABLE participants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    status INTEGER DEFAULT 1,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    version INTEGER DEFAULT 0,
    metadata TEXT
);

CREATE TABLE participant_events (
    id BIGSERIAL PRIMARY KEY,
    participant_id BIGINT REFERENCES participants(id),
    event_id BIGINT REFERENCES events(id),
    event_location_id BIGINT REFERENCES event_locations(id),
    total_spins INTEGER DEFAULT 0,
    remaining_spins INTEGER DEFAULT 0,
    initial_spins INTEGER DEFAULT 0,
    daily_spins_used INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    version INTEGER DEFAULT 0,
    metadata TEXT
);

CREATE TABLE rewards (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    event_id BIGINT REFERENCES events(id),
    status INTEGER DEFAULT 1,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    version INTEGER DEFAULT 0,
    metadata TEXT
);

CREATE TABLE spin_histories (
    id BIGSERIAL PRIMARY KEY,
    participant_event_id BIGINT REFERENCES participant_events(id),
    reward_id BIGINT REFERENCES rewards(id),
    win BOOLEAN DEFAULT FALSE,
    points_earned INTEGER DEFAULT 0,
    finalized BOOLEAN DEFAULT FALSE,
    spin_time TIMESTAMP,
    status INTEGER DEFAULT 1,
    created_date TIMESTAMP,
    last_modified_date TIMESTAMP,
    version INTEGER DEFAULT 0,
    metadata TEXT
);
