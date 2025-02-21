-- Clean up all tables in reverse order of dependencies
TRUNCATE TABLE spin_histories CASCADE;
TRUNCATE TABLE golden_hours CASCADE;
TRUNCATE TABLE rewards CASCADE;
TRUNCATE TABLE participants CASCADE;
TRUNCATE TABLE event_locations CASCADE;
TRUNCATE TABLE events CASCADE;
TRUNCATE TABLE user_roles CASCADE;
TRUNCATE TABLE roles CASCADE;
TRUNCATE TABLE users CASCADE;

-- Reset sequences
ALTER SEQUENCE spin_histories_id_seq RESTART WITH 1;
ALTER SEQUENCE golden_hours_id_seq RESTART WITH 1;
ALTER SEQUENCE rewards_id_seq RESTART WITH 1;
ALTER SEQUENCE participants_id_seq RESTART WITH 1;
ALTER SEQUENCE event_locations_id_seq RESTART WITH 1;
ALTER SEQUENCE events_id_seq RESTART WITH 1;
ALTER SEQUENCE roles_id_seq RESTART WITH 1;
ALTER SEQUENCE users_id_seq RESTART WITH 1;