-- Insert test regions
INSERT INTO regions (name, code, default_win_probability, status) VALUES 
('Test Region 1', 'TR1', 0.3, 1),
('Test Region 2', 'TR2', 0.4, 1),
('Inactive Region', 'IR1', 0.2, 0);

-- Insert test provinces
INSERT INTO provinces (name, code, default_win_probability, status, region_id) VALUES 
('Test Province 1', 'TP1', 0.25, 1, 1),
('Test Province 2', 'TP2', 0.35, 1, 1),
('Inactive Province', 'IP1', 0.15, 0, 1);

-- Insert test event locations
INSERT INTO event_locations (name, code, status, region_id) VALUES 
('Test Location 1', 'TL1', 1, 1),
('Test Location 2', 'TL2', 1, 1),
('Inactive Location', 'IL1', 0, 1);
