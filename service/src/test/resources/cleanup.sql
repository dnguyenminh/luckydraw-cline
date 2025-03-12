-- Clean up data in reverse order of dependencies
DELETE FROM spin_histories;
DELETE FROM rewards;
DELETE FROM event_locations;
DELETE FROM events;
DELETE FROM regions;

-- Reset sequences
ALTER SEQUENCE spin_histories_id_seq RESTART WITH 1;
ALTER SEQUENCE rewards_id_seq RESTART WITH 1;
ALTER SEQUENCE event_locations_id_seq RESTART WITH 1;
ALTER SEQUENCE events_id_seq RESTART WITH 1;
ALTER SEQUENCE regions_id_seq RESTART WITH 1;
