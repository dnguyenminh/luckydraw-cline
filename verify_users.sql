-- Default credentials:
-- admin/admin123 -> $2a$10$rTb6xaVpyU.C1yNXs2gxaOYJ3rp7sAF.bZ5JqHU6L22gYaIS.gkK2
-- user/user123 -> $2a$10$HtVuAPfBbYDQfO7rTLUC4uCHgZ9.P4L4vBQsXJXQ9Enp0P8jFUolu

-- Check if roles exist
SELECT name, id FROM roles ORDER BY id;

-- Check if users exist and are enabled
SELECT id, username, email, enabled,
       substring(password, 1, 20) as password_start
FROM users
ORDER BY id;

-- Check user-role assignments
SELECT u.username, string_agg(r.name, ', ') as roles
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON r.id = ur.role_id
GROUP BY u.username
ORDER BY u.username;

-- Check specific user (admin) details
SELECT u.username,
       u.enabled,
       string_agg(r.name, ', ') as roles,
       u.created_date,
       u.last_modified_date
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON r.id = ur.role_id
WHERE u.username = 'admin'
GROUP BY u.username, u.enabled, u.created_date, u.last_modified_date;

-- Count role assignments
SELECT r.name, COUNT(ur.user_id) as user_count
FROM roles r
LEFT JOIN user_roles ur ON r.id = ur.role_id
GROUP BY r.name
ORDER BY r.name;