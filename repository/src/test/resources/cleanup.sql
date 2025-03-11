-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Clean up all test data
TRUNCATE TABLE event_location;
TRUNCATE TABLE province;
TRUNCATE TABLE region;

-- Reset auto-increment counters
ALTER TABLE event_location AUTO_INCREMENT = 1;
ALTER TABLE province AUTO_INCREMENT = 1;
ALTER TABLE region AUTO_INCREMENT = 1;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
