CREATE TABLE event_locations (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    province VARCHAR(100) NOT NULL,
    total_spins BIGINT NOT NULL DEFAULT 0,
    remaining_spins BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_locations_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

CREATE INDEX idx_event_locations_event ON event_locations(event_id);
CREATE INDEX idx_event_locations_province ON event_locations(province);