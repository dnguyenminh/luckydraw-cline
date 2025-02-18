CREATE TABLE events (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_events_date_range ON events(start_date, end_date);
CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_is_active ON events(is_active);

CREATE TABLE participants (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    event_id BIGINT REFERENCES events(id),
    name VARCHAR(255),
    employee_id VARCHAR(50),
    full_name VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    department VARCHAR(255),
    position VARCHAR(255),
    email VARCHAR(255),
    phone_number VARCHAR(50),
    province VARCHAR(255),
    customer_id VARCHAR(50),
    card_number VARCHAR(50),
    daily_spin_limit INT DEFAULT 0,
    spins_remaining INT DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_participants_event_id ON participants(event_id);
CREATE INDEX idx_participants_employee_id ON participants(employee_id);
CREATE INDEX idx_participants_customer_id ON participants(customer_id);
CREATE INDEX idx_participants_is_active ON participants(is_active);