-- Insert default roles
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'Administrator role'),
('ROLE_USER', 'Regular user role')
ON CONFLICT (name) DO NOTHING;

-- Insert default admin user
INSERT INTO users (username, password, email, first_name, last_name, enabled, created_at, updated_at, created_by, last_modified_by)
VALUES (
    'admin', 
    '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 
    'admin@example.com',
    'Admin',
    'User',
    true, 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP,
    'system',
    'system'
)
ON CONFLICT (username) DO NOTHING;

-- Assign both roles to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u 
CROSS JOIN roles r 
WHERE u.username = 'admin' 
  AND r.name IN ('ROLE_ADMIN', 'ROLE_USER')
ON CONFLICT DO NOTHING;