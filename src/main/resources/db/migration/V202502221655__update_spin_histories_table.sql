-- Add new columns to spin_histories
ALTER TABLE spin_histories
    ADD COLUMN spin_code VARCHAR(50),
    ADD COLUMN is_win BOOLEAN DEFAULT FALSE,
    ADD COLUMN event_location_id BIGINT,
    ADD COLUMN golden_hour_id BIGINT;

-- Add new foreign key constraints
ALTER TABLE spin_histories
    ADD CONSTRAINT fk_spin_histories_event_location 
    FOREIGN KEY (event_location_id) 
    REFERENCES event_locations(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_spin_histories_golden_hour 
    FOREIGN KEY (golden_hour_id) 
    REFERENCES golden_hours(id) ON DELETE SET NULL;

-- Add new indexes
CREATE INDEX idx_spin_histories_event_location ON spin_histories(event_location_id);
CREATE INDEX idx_spin_histories_golden_hour ON spin_histories(golden_hour_id);
CREATE INDEX idx_spin_histories_spin_code ON spin_histories(spin_code);
CREATE INDEX idx_spin_histories_is_win ON spin_histories(is_win);