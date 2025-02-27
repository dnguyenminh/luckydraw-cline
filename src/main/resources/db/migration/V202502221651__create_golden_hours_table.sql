CREATE TABLE golden_hours (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    reward_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    multiplier DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    is_active BOOLEAN DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_golden_hours_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_golden_hours_reward FOREIGN KEY (reward_id) REFERENCES rewards(id) ON DELETE CASCADE
);

CREATE INDEX idx_golden_hours_event ON golden_hours(event_id);
CREATE INDEX idx_golden_hours_reward ON golden_hours(reward_id);
CREATE INDEX idx_golden_hours_time_range ON golden_hours(start_time, end_time);
CREATE INDEX idx_golden_hours_is_active ON golden_hours(is_active);