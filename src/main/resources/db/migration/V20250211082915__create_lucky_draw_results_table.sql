CREATE TABLE lucky_draw_results
(
    id              BIGSERIAL PRIMARY KEY,
    event_id        BIGINT    NOT NULL,
    participant_id  BIGINT    NOT NULL,
    reward_id       BIGINT    NOT NULL,
    spin_history_id BIGINT    NOT NULL,
    win_time        TIMESTAMP NOT NULL,
    pack_number     INTEGER,
    is_claimed      BOOLEAN DEFAULT FALSE,
    claimed_at      TIMESTAMP,
    claimed_by      VARCHAR(255),
    claim_notes     TEXT,
    version         INTEGER DEFAULT 0,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events (id),
    FOREIGN KEY (participant_id) REFERENCES participants (id),
    FOREIGN KEY (reward_id) REFERENCES rewards (id),
    FOREIGN KEY (spin_history_id) REFERENCES spin_histories (id)
);