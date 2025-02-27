-- Insert test users
INSERT INTO users (username, email, first_name, last_name, phone_number, status, enabled)
VALUES ('testuser', 'test@example.com', 'Test', 'User', '0123456789', 'ACTIVE', true);

-- Insert test events
INSERT INTO events (code, name, description, start_date, end_date, total_spins, remaining_spins, is_active, version)
VALUES 
('TEST-EVENT-1', 'Test Event 1', 'Description 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days', 1000, 1000, true, 0),
('TEST-EVENT-2', 'Test Event 2', 'Description 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days', 1000, 1000, true, 0);

-- Insert test event locations
INSERT INTO event_locations (event_id, name, province, total_spins, remaining_spins, daily_spin_limit, spins_remaining, is_active, version)
VALUES 
(1, 'Location 1', 'Province 1', 100, 100, 3, 1000, true, 0),
(1, 'Location 2', 'Province 2', 100, 100, 3, 1000, true, 0),
(2, 'Location 3', 'Province 3', 100, 100, 3, 1000, true, 0);

-- Insert test rewards
INSERT INTO rewards (event_id, name, description, quantity, remaining_quantity, probability, location, province, is_active, version)
VALUES 
(1, 'Reward 1', 'Description 1', 100, 100, 0.5, 'Location 1', 'Province 1', true, 0),
(1, 'Reward 2', 'Description 2', 100, 100, 0.3, 'Location 2', 'Province 2', true, 0),
(2, 'Reward 3', 'Description 3', 100, 100, 0.2, 'Location 3', 'Province 3', true, 0);

-- Insert test golden hours
INSERT INTO golden_hours (event_id, reward_id, name, start_time, end_time, multiplier, is_active, version)
VALUES 
(1, 1, 'Golden Hour 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '1 hour', 2.0, true, 0),
(1, 2, 'Golden Hour 2', CURRENT_TIMESTAMP + INTERVAL '2 hours', CURRENT_TIMESTAMP + INTERVAL '3 hours', 1.5, true, 0);

-- Insert test roles
INSERT INTO roles (name, description)
VALUES 
('ROLE_USER', 'Regular user role'),
('ROLE_ADMIN', 'Administrator role');