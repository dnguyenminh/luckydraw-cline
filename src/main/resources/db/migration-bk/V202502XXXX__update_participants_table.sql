-- Add participant_roles table for direct role management
CREATE TABLE participant_roles (
    participant_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (participant_id, role_id),
    FOREIGN KEY (participant_id) REFERENCES participants (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- Drop old roles foreign key if exists
ALTER TABLE participants DROP CONSTRAINT IF EXISTS fk_participant_roles;

-- Update participants table structure
ALTER TABLE participants
    ADD COLUMN IF NOT EXISTS user_id BIGINT,
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS is_eligible_for_spin BOOLEAN DEFAULT true,
    ADD CONSTRAINT fk_participant_user FOREIGN KEY (user_id) REFERENCES users (id);

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_participant_user ON participants (user_id);
CREATE INDEX IF NOT EXISTS idx_participant_roles ON participant_roles (participant_id);
CREATE INDEX IF NOT EXISTS idx_participant_email ON participants (email);
CREATE INDEX IF NOT EXISTS idx_participant_customer_id ON participants (customer_id);