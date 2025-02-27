-- Add new columns to spin_histories table to consolidate lucky draw functionality
ALTER TABLE spin_histories 
    ADD COLUMN claim_time TIMESTAMP,
    ADD COLUMN is_claimed BOOLEAN DEFAULT false,
    ADD COLUMN claim_location VARCHAR(50),
    ADD COLUMN package_number INTEGER,
    ADD COLUMN claim_code VARCHAR(50),
    ADD COLUMN claim_notes TEXT;

-- Create indexes for common queries
CREATE INDEX idx_spin_histories_is_claimed ON spin_histories(is_claimed);
CREATE INDEX idx_spin_histories_claim_time ON spin_histories(claim_time);
CREATE INDEX idx_spin_histories_package_number ON spin_histories(package_number);

-- Add combined index for finding unclaimed rewards
CREATE INDEX idx_spin_histories_won_claimed ON spin_histories(won, is_claimed);

-- Clean up any existing references to lucky_draw_results
DROP TABLE IF EXISTS lucky_draw_results;

-- Comment explaining the consolidation
COMMENT ON TABLE spin_histories IS 'Consolidated spin history and lucky draw tracking table';