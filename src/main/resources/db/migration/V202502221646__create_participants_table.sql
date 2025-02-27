CREATE TABLE participants (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT,
    customer_id VARCHAR(50) NOT NULL UNIQUE,
    card_number VARCHAR(16) NOT NULL,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    province VARCHAR(100) NOT NULL,
    daily_spin_limit BIGINT NOT NULL DEFAULT 3,
    spins_remaining BIGINT NOT NULL DEFAULT 3,
    is_active BOOLEAN DEFAULT true,
    is_eligible_for_spin BOOLEAN DEFAULT true,
    last_spin_date TIMESTAMP,
    created_by VARCHAR(255),
    modified_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_participants_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_participants_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_participants_event ON participants(event_id);
CREATE INDEX idx_participants_user ON participants(user_id);
CREATE INDEX idx_participants_card_number ON participants(card_number);
CREATE INDEX idx_participants_customer_id ON participants(customer_id);
CREATE INDEX idx_participants_email ON participants(email);