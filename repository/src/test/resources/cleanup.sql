-- Clean all tables in reverse order of dependencies
TRUNCATE TABLE spin_histories CASCADE;
TRUNCATE TABLE golden_hours CASCADE;
TRUNCATE TABLE rewards CASCADE;
TRUNCATE TABLE participant_events CASCADE;
TRUNCATE TABLE user_roles CASCADE;
TRUNCATE TABLE event_provinces CASCADE;
TRUNCATE TABLE event_locations CASCADE;
TRUNCATE TABLE provinces CASCADE;
TRUNCATE TABLE events CASCADE;
TRUNCATE TABLE regions CASCADE;
TRUNCATE TABLE roles CASCADE;
TRUNCATE TABLE users CASCADE;

-- Reset sequences
ALTER SEQUENCE spin_histories_id_seq RESTART WITH 1;
ALTER SEQUENCE golden_hours_id_seq RESTART WITH 1;
ALTER SEQUENCE rewards_id_seq RESTART WITH 1;
ALTER SEQUENCE participant_events_id_seq RESTART WITH 1;
ALTER SEQUENCE event_locations_id_seq RESTART WITH 1;
ALTER SEQUENCE provinces_id_seq RESTART WITH 1;
ALTER SEQUENCE events_id_seq RESTART WITH 1;
ALTER SEQUENCE regions_id_seq RESTART WITH 1;
ALTER SEQUENCE roles_id_seq RESTART WITH 1;
ALTER SEQUENCE users_id_seq RESTART WITH 1;
