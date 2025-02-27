-- Create golden hours table
CREATE TABLE IF NOT EXISTS golden_hours (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    multiplier DECIMAL(4,2) NOT NULL DEFAULT 1.0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_golden_hour_event FOREIGN KEY (event_id) REFERENCES events(id)
);

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_golden_hour_event ON golden_hours(event_id);
CREATE INDEX IF NOT EXISTS idx_golden_hour_time ON golden_hours(start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_golden_hour_active ON golden_hours(is_active);