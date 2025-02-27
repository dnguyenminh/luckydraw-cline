CREATE TABLE spin_histories
(
    id                 BIGSERIAL PRIMARY KEY,
    event_id           BIGINT    NOT NULL,
    participant_id     BIGINT    NOT NULL,
    reward_id          BIGINT,
    spin_time          TIMESTAMP NOT NULL,
    result             TEXT,
    won                BOOLEAN DEFAULT false,
    remaining_spins    INTEGER   NOT NULL,
    current_multiplier INTEGER DEFAULT 1,
    is_golden_hour     BOOLEAN DEFAULT false,
    created_at         TIMESTAMP,
    updated_at         TIMESTAMP,
    version            INTEGER DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES events (id),
    FOREIGN KEY (participant_id) REFERENCES participants (id),
    FOREIGN KEY (reward_id) REFERENCES rewards (id)
);