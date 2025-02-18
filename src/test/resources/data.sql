-- Insert roles
INSERT INTO roles (name, description) VALUES
('ROLE_USER', 'Regular user role'),
('ROLE_ADMIN', 'Administrator role');

-- Insert test users with encoded passwords
-- Password is 'password123' encoded with BCrypt
INSERT INTO users (email, username, password, first_name, last_name, enabled, created_at, updated_at)
VALUES 
('admin@example.com', 'admin', '$2a$10$sMQAEI2oYvmF.lUkA7v2/.sJzoZsyz6yOGHHWeGV3NQS/FxNlLE4a', 'Admin', 'User', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user@example.com', 'user', '$2a$10$sMQAEI2oYvmF.lUkA7v2/.sJzoZsyz6yOGHHWeGV3NQS/FxNlLE4a', 'Regular', 'User', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u 
CROSS JOIN roles r 
WHERE (u.username = 'admin' AND r.name IN ('ROLE_USER', 'ROLE_ADMIN'))
   OR (u.username = 'user' AND r.name = 'ROLE_USER');