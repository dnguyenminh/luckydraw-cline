-- Create event_locations table if not exists
CREATE TABLE IF NOT EXISTS event_locations (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id),
    location VARCHAR(100) NOT NULL,
    total_spins INT NOT NULL,
    remaining_spins INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(event_id, location)
);

-- Add event_location_id column to participants table if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'participants' 
        AND column_name = 'event_location_id'
    ) THEN
        ALTER TABLE participants 
        ADD COLUMN event_location_id BIGINT,
        ADD CONSTRAINT fk_participants_event_location 
        FOREIGN KEY (event_location_id) 
        REFERENCES event_locations(id);
    END IF;
END $$;

-- Create indexes if they don't exist
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE indexname = 'idx_event_locations_event'
    ) THEN
        CREATE INDEX idx_event_locations_event ON event_locations(event_id);
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE indexname = 'idx_participants_event_location'
    ) THEN
        CREATE INDEX idx_participants_event_location ON participants(event_location_id);
    END IF;
END $$;