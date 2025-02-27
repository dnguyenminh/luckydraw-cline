-- Update Reward table to add location fields
ALTER TABLE rewards
    ADD COLUMN location VARCHAR(255),
    ADD COLUMN province VARCHAR(100);

-- Create index for reward province
CREATE INDEX idx_rewards_province ON rewards(province);

-- Update EventLocation table
ALTER TABLE event_locations
    ALTER COLUMN total_spins TYPE INT,
    ALTER COLUMN remaining_spins TYPE INT,
    ADD COLUMN daily_spin_limit INT DEFAULT 3,
    ADD COLUMN spins_remaining BIGINT DEFAULT 0;

-- Convert existing BIGINT values to INT for total_spins and remaining_spins
UPDATE event_locations 
SET total_spins = CAST(total_spins AS INT),
    remaining_spins = CAST(remaining_spins AS INT);

-- Add new index for spins_remaining
CREATE INDEX idx_event_locations_spins_remaining ON event_locations(spins_remaining);