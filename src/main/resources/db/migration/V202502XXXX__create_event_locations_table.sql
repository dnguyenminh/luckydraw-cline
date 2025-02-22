CREATE TABLE event_locations (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    location VARCHAR(255),
    name VARCHAR(100),
    total_spins BIGINT NOT NULL,
    remaining_spins BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_event_location_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);