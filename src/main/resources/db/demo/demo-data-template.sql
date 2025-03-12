-- Initial administrative staff users
INSERT INTO users (
    id, username, password, first_name, last_name, email, phone_number, position,
    account_non_locked, account_non_expired, credentials_non_expired,
    failed_attempts, account_locked, password_expired,
    status, version, created_at, created_by
)
VALUES 
    -- Administrative staff
    (1, 'admin', '{DEFAULT_PASSWORD}', 'System', 'Administrator', 'admin@system.com', '0123456789', 'Administrator',
     true, true, true, 0, false, false, 1, 0, '{NOW}', 'system'),
    (2, 'operator1', '{DEFAULT_PASSWORD}', 'First', 'Operator', 'operator1@system.com', '0123456781', 'Operator',
     true, true, true, 0, false, false, 1, 0, '{NOW}', 'system'),
    (3, 'operator2', '{DEFAULT_PASSWORD}', 'Second', 'Operator', 'operator2@system.com', '0123456782', 'Operator',
     true, true, true, 0, false, false, 1, 0, '{NOW}', 'system'),
    (4, 'operator3', '{DEFAULT_PASSWORD}', 'Third', 'Operator', 'operator3@system.com', '0123456783', 'Operator',
     true, true, true, 0, false, false, 1, 0, '{NOW}', 'system'),
    (5, 'manager1', '{DEFAULT_PASSWORD}', 'First', 'Manager', 'manager1@system.com', '0123456784', 'Manager',
     true, true, true, 0, false, false, 1, 0, '{NOW}', 'system'),
    (6, 'manager2', '{DEFAULT_PASSWORD}', 'Second', 'Manager', 'manager2@system.com', '0123456785', 'Manager',
     true, true, true, 0, false, false, 1, 0, '{NOW}', 'system');

-- Generate users for participants
WITH RECURSIVE user_gen AS (
    SELECT 7 as id, 1 as counter  -- Start after admin users
    UNION ALL
    SELECT id + 1, counter + 1
    FROM user_gen 
    WHERE counter < 1500
)
INSERT INTO users (
    id, username, password, first_name, last_name, email, phone_number,
    account_non_locked, account_non_expired, credentials_non_expired,
    failed_attempts, account_locked, password_expired,
    status, version, created_at, created_by
)
SELECT 
    id,
    'user' || LPAD(counter::text, 4, '0') as username,
    '{DEFAULT_PASSWORD}',
    'User',
    LPAD(counter::text, 4, '0'),
    'user' || LPAD(counter::text, 4, '0') || '@example.com',
    '09' || LPAD(counter::text, 8, '0'),
    true, true, true, 0, false, false,
    1, 0,
    CASE 
        WHEN counter < 500 THEN '{PAST_1Y}'   -- Old users
        WHEN counter < 1000 THEN '{PAST_6M}'  -- Recent users
        ELSE '{NOW}'                          -- New users
    END,
    'system'
FROM user_gen;

-- System roles (using RoleName enum values)
INSERT INTO roles (id, name, description, status, version, created_at, created_by)
VALUES 
    -- Administrative roles
    (1, 'ROLE_ADMIN', 'System Administrator', 1, 0, '{NOW}', 'system'),
    (2, 'ROLE_EVENT_MANAGER', 'Event management and operations', 1, 0, '{NOW}', 'system'),
    (3, 'ROLE_REWARD_MANAGER', 'Reward management access', 1, 0, '{NOW}', 'system'),
    (4, 'ROLE_PARTICIPANT_MANAGER', 'Participant management access', 1, 0, '{NOW}', 'system'),
    -- Operational roles
    (5, 'ROLE_OPERATOR', 'System operations access', 1, 0, '{NOW}', 'system'),
    (6, 'ROLE_MODERATOR', 'Content moderation access', 1, 0, '{NOW}', 'system'),
    (7, 'ROLE_VIEWER', 'View-only access', 1, 0, '{NOW}', 'system'),
    -- User roles
    (8, 'ROLE_USER', 'Basic authenticated user', 1, 0, '{NOW}', 'system'),
    (9, 'ROLE_PARTICIPANT', 'Event participant access', 1, 0, '{NOW}', 'system');

-- Administrative staff role assignments
INSERT INTO user_roles (user_id, role_id)
VALUES 
    -- Administrative staff roles
    (1, 1),  -- admin -> ROLE_ADMIN
    (1, 2),  -- admin -> ROLE_EVENT_MANAGER
    (1, 3),  -- admin -> ROLE_REWARD_MANAGER
    (1, 4),  -- admin -> ROLE_PARTICIPANT_MANAGER
    (2, 5),  -- operator1 -> ROLE_OPERATOR
    (3, 5),  -- operator2 -> ROLE_OPERATOR
    (4, 5),  -- operator3 -> ROLE_OPERATOR
    (5, 2),  -- manager1 -> ROLE_EVENT_MANAGER
    (6, 2);  -- manager2 -> ROLE_EVENT_MANAGER

-- Assign basic roles to all generated users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, 8  -- role_id 8 is ROLE_USER
FROM users u 
WHERE u.id > 6;  -- All users after admin users

-- Geographical regions
INSERT INTO regions (id, code, name, description, status, version, created_at, created_by)
VALUES 
    (1, 'NORTH', 'Northern Region', 'Northern Vietnam region including Hanoi', 1, 0, '{NOW}', 'system'),
    (2, 'CENTRAL', 'Central Region', 'Central Vietnam region including Da Nang', 1, 0, '{NOW}', 'system'),
    (3, 'SOUTH', 'Southern Region', 'Southern Vietnam region including HCMC', 1, 0, '{NOW}', 'system');

-- Key provinces
INSERT INTO provinces (id, region_id, code, name, status, version, created_at, created_by)
VALUES
    -- Northern provinces
    (1, 1, 'HN', 'Hanoi', 1, 0, '{NOW}', 'system'),
    (2, 1, 'HP', 'Hai Phong', 1, 0, '{NOW}', 'system'),
    -- Central provinces
    (3, 2, 'DN', 'Da Nang', 1, 0, '{NOW}', 'system'),
    (4, 2, 'HUE', 'Thua Thien Hue', 1, 0, '{NOW}', 'system'),
    -- Southern provinces
    (5, 3, 'HCM', 'Ho Chi Minh City', 1, 0, '{NOW}', 'system'),
    (6, 3, 'CT', 'Can Tho', 1, 0, '{NOW}', 'system');

-- Events across time periods
INSERT INTO events (id, code, name, description, start_time, end_time, initial_spins, daily_spin_limit,
                    default_win_probability, status, version, created_at, created_by)
WITH event_data AS (
    -- Past Events (completed)
    SELECT 1, 'TET2022', 'Tet Holiday 2022', 'Tet celebration event', '{PAST_3Y}', '{PAST_2Y}', 10, 5, 0.1, 2, 0 UNION ALL
    SELECT 2, 'SUMMER2022', 'Summer Festival 2022', 'Summer beach event', '{PAST_2Y}', '{PAST_1Y}', 8, 4, 0.15, 2, 0 UNION ALL
    SELECT 3, 'TET2023', 'Tet Holiday 2023', 'Tet celebration event', '{PAST_2Y}', '{PAST_1Y}', 10, 5, 0.1, 2, 0 UNION ALL
    SELECT 4, 'SUMMER2023', 'Summer Festival 2023', 'Summer beach event', '{PAST_1Y}', '{PAST_6M}', 8, 4, 0.15, 2, 0 UNION ALL
    
    -- Current Active Events
    SELECT 5, 'TET2024', 'Tet Holiday 2024', 'Current Tet celebration', '{PAST_1M}', '{FUTURE_1M}', 10, 5, 0.1, 1, 0 UNION ALL
    SELECT 6, 'SPRING2024', 'Spring Festival 2024', 'Spring celebration', '{NOW}', '{FUTURE_3M}', 12, 6, 0.12, 1, 0 UNION ALL
    SELECT 7, 'SUMMER2024', 'Summer Festival 2024', 'Summer beach party', '{FUTURE_1M}', '{FUTURE_6M}', 8, 4, 0.15, 1, 0 UNION ALL
    
    -- Future Planned Events
    SELECT 8, 'AUTUMN2024', 'Autumn Festival 2024', 'Fall celebration', '{FUTURE_3M}', '{FUTURE_6M}', 10, 5, 0.1, 1, 0 UNION ALL
    SELECT 9, 'WINTER2024', 'Winter Festival 2024', 'Winter celebration', '{FUTURE_6M}', '{FUTURE_1Y}', 15, 7, 0.08, 1, 0 UNION ALL
    SELECT 10, 'TET2025', 'Tet Holiday 2025', 'Next year Tet event', '{FUTURE_1Y}', '{FUTURE_2Y}', 10, 5, 0.1, 1, 0
)
SELECT 
    id, code, name, description, start_time, end_time, initial_spins, daily_spin_limit,
    default_win_probability, status, version, '{NOW}' as created_at, 'system' as created_by
FROM event_data;

-- Event Locations (1000+ records across regions)
INSERT INTO event_locations (event_id, region_id, code, name, description, initial_spins, daily_spin_limit,
                           default_win_probability, status, version, created_at, created_by)
WITH RECURSIVE location_gen AS (
    -- Base case: Start with event 1, region 1
    SELECT 
        1 as event_id, 
        1 as region_id, 
        1 as counter
    
    UNION ALL
    
    -- Recursive case: Generate next combination
    SELECT 
        CASE 
            WHEN region_id = 3 THEN event_id + 1
            ELSE event_id 
        END,
        CASE 
            WHEN region_id = 3 THEN 1 
            ELSE region_id + 1
        END,
        counter + 1
    FROM location_gen 
    WHERE counter < 1200
)
SELECT 
    event_id,
    region_id,
    'LOC' || LPAD(counter::text, 4, '0') as code,
    'Location ' || counter as name,
    'Event location ' || counter || ' description' as description,
    10 as initial_spins,
    5 as daily_spin_limit,
    0.1 as default_win_probability,
    1 as status,
    0 as version,
    '{NOW}' as created_at,
    'system' as created_by
FROM location_gen;

-- Generate participants with user links
WITH RECURSIVE participant_gen AS (
    SELECT 1 as counter
    UNION ALL
    SELECT counter + 1
    FROM participant_gen 
    WHERE counter < 1500
)
INSERT INTO participants (code, name, phone, email, account, user_id, province_id, status, version, created_at, created_by)
SELECT 
    'PART' || LPAD(counter::text, 4, '0') as code,
    'Participant ' || counter as name,
    '0' || (1000000000 + counter)::text as phone,
    'participant' || counter || '@example.com' as email,
    'USER' || LPAD(counter::text, 4, '0') as account,
    counter + 6 as user_id,  -- Link to generated users
    (counter % 6) + 1 as province_id,
    1 as status,
    0 as version,
    CASE 
        WHEN counter < 500 THEN '{PAST_1Y}'   -- Old participants
        WHEN counter < 1000 THEN '{PAST_6M}'  -- Recent participants
        ELSE '{NOW}'                          -- New participants
    END as created_at,
    'system' as created_by
FROM participant_gen;

-- Assign participant role
INSERT INTO participant_roles (participant_id, role_id)
SELECT
    p.id as participant_id,
    CASE 
        WHEN p.id % 10 = 0 THEN 9  -- Every 10th participant is ROLE_PARTICIPANT
        ELSE 8                      -- Others are ROLE_USER
    END as role_id
FROM participants p;

-- Generate rewards for events
INSERT INTO rewards (event_location_id, code, name, description, points, points_required, 
                    total_quantity, remaining_quantity, daily_limit, win_probability,
                    valid_from, valid_until, status, version, created_at, created_by)
WITH RECURSIVE reward_gen AS (
    SELECT 1 as counter
    UNION ALL
    SELECT counter + 1 FROM reward_gen WHERE counter < 100
)
SELECT 
    (counter % 1200) + 1 as event_location_id,
    'REW' || LPAD(counter::text, 4, '0') as code,
    CASE counter % 4
        WHEN 0 THEN 'Gold Prize - ' || counter
        WHEN 1 THEN 'Silver Prize - ' || counter
        WHEN 2 THEN 'Bronze Prize - ' || counter
        ELSE 'Special Prize - ' || counter
    END as name,
    'Reward description for ' || counter as description,
    (counter % 10 + 1) * 100 as points,
    (counter % 5 + 1) * 50 as points_required,
    100 - (counter % 50) as total_quantity,
    100 - (counter % 50) as remaining_quantity,
    counter % 5 + 1 as daily_limit,
    0.1 / (counter % 10 + 1) as win_probability,
    CASE 
        WHEN counter < 30 THEN '{PAST_1Y}'
        WHEN counter < 60 THEN '{NOW}'
        ELSE '{FUTURE_1M}'
    END as valid_from,
    CASE 
        WHEN counter < 30 THEN '{PAST_6M}'
        WHEN counter < 60 THEN '{FUTURE_6M}'
        ELSE '{FUTURE_1Y}'
    END as valid_until,
    1 as status,
    0 as version,
    '{NOW}' as created_at,
    'system' as created_by
FROM reward_gen;

-- Generate golden hours
WITH RECURSIVE golden_hour_gen AS (
    SELECT 1 as counter
    UNION ALL
    SELECT counter + 1 FROM golden_hour_gen WHERE counter < 200
)
INSERT INTO golden_hours (event_location_id, name, description, start_time, end_time,
                         win_probability, status, version, created_at, created_by)
SELECT 
    (counter % 1200) + 1 as event_location_id,
    CASE counter % 3
        WHEN 0 THEN 'Morning Rush - ' || counter
        WHEN 1 THEN 'Lunch Special - ' || counter
        ELSE 'Evening Prime - ' || counter
    END as name,
    'Golden hour description for ' || counter as description,
    CASE 
        WHEN counter < 50 THEN '{PAST_1Y}'
        WHEN counter < 100 THEN '{NOW}'
        ELSE '{FUTURE_1M}'
    END as start_time,
    CASE 
        WHEN counter < 50 THEN '{PAST_6M}'
        WHEN counter < 100 THEN '{FUTURE_3M}'
        ELSE '{FUTURE_6M}'
    END as end_time,
    0.2 + (counter % 10) * 0.01 as win_probability,
    1 as status,
    0 as version,
    '{NOW}' as created_at,
    'system' as created_by
FROM golden_hour_gen;

-- Generate participant event registrations
WITH RECURSIVE reg_gen AS (
    SELECT 1 as counter
    UNION ALL
    SELECT counter + 1 FROM reg_gen WHERE counter < 2000
)
INSERT INTO participant_events (participant_id, event_location_id, total_spins, available_spins, daily_spin_count,
                              total_wins, total_points, status, version, created_at, created_by)
SELECT
    (counter % 1500) + 1 as participant_id,
    (counter % 1200) + 1 as event_location_id,
    CASE 
        WHEN counter % 10 = 0 THEN 15  -- VIP participants get more spins
        ELSE 10
    END as total_spins,
    CASE 
        WHEN counter % 10 = 0 THEN 15 - (counter % 5)
        ELSE 10 - (counter % 5)
    END as available_spins,
    counter % 5 as daily_spin_count,
    CASE 
        WHEN counter % 3 = 0 THEN counter % 5
        ELSE 0
    END as total_wins,
    CASE 
        WHEN counter % 3 = 0 THEN (counter % 5 + 1) * 100
        ELSE 0
    END as total_points,
    1 as status,
    0 as version,
    CASE 
        WHEN counter < 700 THEN '{PAST_1Y}'
        WHEN counter < 1400 THEN '{PAST_6M}'
        ELSE '{NOW}'
    END as created_at,
    'system' as created_by
FROM reg_gen;

-- Generate spin histories
WITH RECURSIVE spin_gen AS (
    SELECT 1 as counter
    UNION ALL
    SELECT counter + 1 FROM spin_gen WHERE counter < 5000
)
INSERT INTO spin_histories (participant_id, event_location_id, reward_id, golden_hour_id, timestamp,
                          win, points_earned, points_spent, status, version, created_at, created_by)
SELECT 
    (counter % 1500) + 1 as participant_id,
    (counter % 1200) + 1 as event_location_id,
    (counter % 100) + 1 as reward_id,
    CASE WHEN counter % 5 = 0 THEN (counter % 200) + 1 ELSE NULL END as golden_hour_id,
    CASE 
        WHEN counter < 2000 THEN '{PAST_1Y}'
        WHEN counter < 3500 THEN '{PAST_6M}'
        ELSE '{NOW}'
    END as timestamp,
    counter % 3 = 0 as win,
    CASE WHEN counter % 3 = 0 THEN (counter % 5 + 1) * 100 ELSE 0 END as points_earned,
    50 as points_spent,
    1 as status,
    0 as version,
    CASE 
        WHEN counter < 2000 THEN '{PAST_1Y}'
        WHEN counter < 3500 THEN '{PAST_6M}'
        ELSE '{NOW}'
    END as created_at,
    'system' as created_by
FROM spin_gen;

-- Reset sequences to account for all generated data
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id) + 1, 1) FROM users), false);
SELECT setval('roles_id_seq', (SELECT COALESCE(MAX(id) + 1, 1) FROM roles), false);
SELECT setval('regions_id_seq', (SELECT COALESCE(MAX(id) + 1, 1) FROM regions), false);
SELECT setval('provinces_id_seq', (SELECT COALESCE(MAX(id) + 1, 1) FROM provinces), false);
SELECT setval('events_id_seq', (SELECT COALESCE(MAX(id) + 1, 1) FROM events), false);
SELECT setval('event_locations_id_seq', (SELECT COALESCE(MAX(id) + 1, 1) FROM event_locations), false);
SELECT setval('participants_id_seq', (SELECT COALESCE(MAX(id) + 1, 1) FROM participants), false);
SELECT setval('rewards_id_seq', (SELECT COALESCE(MAX(id) + 1, 1) FROM rewards), false);
SELECT setval('golden_hours_id_seq', (SELECT COALESCE(MAX(id) + 1, 1) FROM golden_hours), false);
SELECT setval('participant_events_id_seq', (SELECT COALESCE(MAX(id) + 1, 1) FROM participant_events), false);
SELECT setval('spin_histories_id_seq', (SELECT COALESCE(MAX(id) + 1, 1) FROM spin_histories), false);
