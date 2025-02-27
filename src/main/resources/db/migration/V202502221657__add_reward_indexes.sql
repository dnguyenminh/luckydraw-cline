-- Add indexes for new fields in rewards table
CREATE INDEX idx_rewards_location ON rewards(location);
CREATE INDEX idx_rewards_province ON rewards(province);