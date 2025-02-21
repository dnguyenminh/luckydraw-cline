CREATE TABLE spin_histories (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    reward_id BIGINT,
    spin_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    result VARCHAR(50),
    won BOOLEAN,
    remaining_spins INT,
    current_multiplier DECIMAL(5,2) NOT NULL DEFAULT 1.0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_event_spin FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_reward_spin FOREIGN KEY (reward_id) REFERENCES rewards(id) ON DELETE CASCADE,
    CONSTRAINT fk_participant_spin FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE
);