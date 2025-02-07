INSERT INTO reward (event_name, name, total_quantity, start_date, end_date, applicable_provinces)
VALUES 
('Summer Event', 'Gift Card', 100, '2023-06-01', '2023-08-31', ARRAY['Hanoi', 'Ho Chi Minh City']),
('Winter Sale', 'Discount Coupon', 200, '2023-12-01', '2024-01-31', ARRAY['All']);
