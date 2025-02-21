-- Insert test roles
INSERT INTO roles (name) VALUES 
('ROLE_USER'),
('ROLE_ADMIN');

-- Insert test users
INSERT INTO users (username, password, email, first_name, last_name, enabled)
VALUES 
('admin', '$2a$10$TUfuGorQhzYr8F.EyEX.g.TGPE0dL9DpKA5y7JUJQQvkEEyduB1.u', 'admin@example.com', 'Admin', 'User', true),
('user', '$2a$10$TUfuGorQhzYr8F.EyEX.g.TGPE0dL9DpKA5y7JUJQQvkEEyduB1.u', 'user@example.com', 'Regular', 'User', true);

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'user' AND r.name = 'ROLE_USER';

-- Insert test events
INSERT INTO events (code, name, description, total_spins, remaining_spins, start_date, end_date, is_active)
VALUES 
('EVENT001', 'Test Event', 'A test event', 1000, 1000, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '30 days', true);

-- Insert test event locations
INSERT INTO event_locations (event_id, name, total_spins, remaining_spins)
SELECT e.id, 'Location 1', 500, 500
FROM events e
WHERE e.code = 'EVENT001';