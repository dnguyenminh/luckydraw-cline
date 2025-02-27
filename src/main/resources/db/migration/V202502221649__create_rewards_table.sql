CREATE TABLE rewards (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    quantity BIGINT NOT NULL DEFAULT 0,
    remaining BIGINT NOT NULL DEFAULT 0,
    probability DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    is_active BOOLEAN DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rewards_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

CREATE INDEX idx_rewards_event ON rewards(event_id);