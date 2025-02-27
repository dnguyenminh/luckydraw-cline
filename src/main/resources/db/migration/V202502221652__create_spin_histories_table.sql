CREATE TABLE spin_histories (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    reward_id BIGINT,
    spin_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_spin_histories_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_spin_histories_participant FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE,
    CONSTRAINT fk_spin_histories_reward FOREIGN KEY (reward_id) REFERENCES rewards(id) ON DELETE SET NULL
);

CREATE INDEX idx_spin_histories_event ON spin_histories(event_id);
CREATE INDEX idx_spin_histories_participant ON spin_histories(participant_id);
CREATE INDEX idx_spin_histories_reward ON spin_histories(reward_id);
CREATE INDEX idx_spin_histories_spin_time ON spin_histories(spin_time);
CREATE INDEX idx_spin_histories_status ON spin_histories(status);