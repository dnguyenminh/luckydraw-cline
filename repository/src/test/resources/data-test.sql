-- Initial regions
INSERT INTO region (id, name, code, default_win_probability, status, created_by, created_at)
VALUES 
(1, 'North Region', 'NORTH', 0.3, 1, 'system', CURRENT_TIMESTAMP),
(2, 'South Region', 'SOUTH', 0.25, 1, 'system', CURRENT_TIMESTAMP),
(3, 'Central Region', 'CENTRAL', 0.2, 0, 'system', CURRENT_TIMESTAMP),
(4, 'Highland Region', 'HIGH', 0.15, 1, 'system', CURRENT_TIMESTAMP);

-- Initial provinces
INSERT INTO province (id, name, code, region_id, default_win_probability, status, created_by, created_at)
VALUES 
(1, 'Hanoi', 'HN', 1, 0.25, 1, 'system', CURRENT_TIMESTAMP),
(2, 'Hai Phong', 'HP', 1, 0.2, 1, 'system', CURRENT_TIMESTAMP),
(3, 'Ho Chi Minh', 'HCM', 2, 0.2, 1, 'system', CURRENT_TIMESTAMP),
(4, 'Can Tho', 'CT', 2, 0.15, 1, 'system', CURRENT_TIMESTAMP),
(5, 'Da Nang', 'DN', 3, 0.15, 0, 'system', CURRENT_TIMESTAMP),
(6, 'Hue', 'HUE', 3, 0.15, 0, 'system', CURRENT_TIMESTAMP);

-- Initial event locations
INSERT INTO event_location (id, name, code, region_id, status, created_by, created_at)
VALUES 
(1, 'Event Center North 1', 'ECN1', 1, 1, 'system', CURRENT_TIMESTAMP),
(2, 'Event Center North 2', 'ECN2', 1, 1, 'system', CURRENT_TIMESTAMP),
(3, 'Event Center South 1', 'ECS1', 2, 1, 'system', CURRENT_TIMESTAMP),
(4, 'Event Center South 2', 'ECS2', 2, 1, 'system', CURRENT_TIMESTAMP),
(5, 'Event Center Central', 'ECC1', 3, 0, 'system', CURRENT_TIMESTAMP),
(6, 'Event Center Highland', 'ECH1', 4, 1, 'system', CURRENT_TIMESTAMP);
