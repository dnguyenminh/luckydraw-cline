-- Clean up all test data
DELETE FROM participant_roles;
DELETE FROM spin_histories;
DELETE FROM golden_hours;
DELETE FROM rewards;
DELETE FROM participants;
DELETE FROM event_locations;
DELETE FROM events;
DELETE FROM roles;
DELETE FROM users;

-- Reset sequences
ALTER SEQUENCE participant_roles_id_seq RESTART WITH 1;
ALTER SEQUENCE spin_histories_id_seq RESTART WITH 1;
ALTER SEQUENCE golden_hours_id_seq RESTART WITH 1;
ALTER SEQUENCE rewards_id_seq RESTART WITH 1;
ALTER SEQUENCE participants_id_seq RESTART WITH 1;
ALTER SEQUENCE event_locations_id_seq RESTART WITH 1;
ALTER SEQUENCE events_id_seq RESTART WITH 1;
ALTER SEQUENCE roles_id_seq RESTART WITH 1;
ALTER SEQUENCE users_id_seq RESTART WITH 1;