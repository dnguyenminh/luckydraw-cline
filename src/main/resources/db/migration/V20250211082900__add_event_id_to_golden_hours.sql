ALTER TABLE golden_hours
ADD COLUMN event_id BIGINT NOT NULL;

ALTER TABLE golden_hours
ADD CONSTRAINT fk_golden_hours_event
FOREIGN KEY (event_id)
REFERENCES events(id);

CREATE INDEX idx_golden_hours_event_id ON golden_hours(event_id);