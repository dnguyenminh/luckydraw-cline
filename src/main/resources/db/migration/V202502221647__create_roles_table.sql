CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_roles_name ON roles(name);

-- Insert default roles
INSERT INTO roles (name, description) 
VALUES 
    ('ROLE_ADMIN', 'Administrator role with full access'),
    ('ROLE_USER', 'Standard user role with basic access'),
    ('ROLE_EVENT_MANAGER', 'Event manager role with event management access'),
    ('ROLE_PARTICIPANT', 'Participant role with event participation access');