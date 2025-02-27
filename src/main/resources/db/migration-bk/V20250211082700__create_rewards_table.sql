CREATE TABLE rewards (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    remaining_quantity INTEGER NOT NULL,
    probability DOUBLE PRECISION NOT NULL,
    golden_hour_probability DOUBLE PRECISION NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    start_date DATE,
    end_date DATE,
    limit_from_date DATE,
    limit_to_date DATE,
    max_quantity_per_period INTEGER NOT NULL DEFAULT 0,
    event_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_rewards_event
        FOREIGN KEY (event_id)
        REFERENCES events(id)
);

CREATE INDEX idx_rewards_event_id ON rewards(event_id);
CREATE INDEX idx_rewards_dates ON rewards(start_date, end_date);
CREATE INDEX idx_rewards_limit_dates ON rewards(limit_from_date, limit_to_date);
CREATE INDEX idx_rewards_is_active ON rewards(is_active);

CREATE INDEX idx_rewards_date_range ON rewards(start_date, end_date);
CREATE INDEX idx_rewards_limit_date_range ON rewards(limit_from_date, limit_to_date);