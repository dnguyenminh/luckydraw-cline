CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    daily_spin_limit BIGINT NOT NULL DEFAULT 3,
    total_spins BIGINT NOT NULL DEFAULT 0,
    remaining_spins BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_event_dates CHECK (end_date > start_date)
);

CREATE INDEX idx_events_code ON events(code);
CREATE INDEX idx_events_dates ON events(start_date, end_date);

-- Insert a test event
INSERT INTO events (code, name, description, start_date, end_date, daily_spin_limit, total_spins, remaining_spins, is_active, version)
VALUES ('TEST_EVENT', 'Test Event', 'Event for testing', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days', 3, 1000, 1000, true, 0);