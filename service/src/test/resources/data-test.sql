-- Insert test region
INSERT INTO regions (id, code, name, status, created_at) 
VALUES (1, 'REGION001', 'Test Region', 1, CURRENT_TIMESTAMP);

-- Insert test event
INSERT INTO events (id, code, name, start_time, end_time, status, created_at)
VALUES (1, 'EVENT001', 'Test Event', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '7 days', 1, CURRENT_TIMESTAMP);

-- Insert test event location
INSERT INTO event_locations (id, event_id, region_id, code, name, status, created_at)
VALUES (1, 1, 1, 'LOC001', 'Test Location', 1, CURRENT_TIMESTAMP);

-- Insert test reward
INSERT INTO rewards (id, event_location_id, code, name, total_quantity, remaining_quantity, win_probability, status, created_at)
VALUES (1, 1, 'REWARD001', 'Test Reward', 100, 100, 0.1, 1, CURRENT_TIMESTAMP);
