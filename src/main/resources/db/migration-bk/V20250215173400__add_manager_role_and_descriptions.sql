-- Add MANAGER role
INSERT INTO roles (name, description) 
VALUES ('MANAGER', 'Manager with event management access')
ON CONFLICT (name) DO NOTHING;

-- Update descriptions for existing roles
UPDATE roles SET description = 'Administrator with full system access' WHERE name = 'ADMIN';
UPDATE roles SET description = 'Regular user with basic access' WHERE name = 'USER';