ALTER TABLE users
    ADD COLUMN department VARCHAR(100),
    ADD COLUMN position VARCHAR(100);

-- Update existing admin user with profile information
UPDATE users 
SET first_name = 'System',
    last_name = 'Administrator',
    department = 'IT',
    position = 'System Administrator'
WHERE username = 'admin';

-- Update existing regular user with profile information
UPDATE users 
SET first_name = 'Regular',
    last_name = 'User',
    department = 'General',
    position = 'User'
WHERE username = 'user';

-- Create an index on the most frequently searched fields
CREATE INDEX idx_users_names ON users(first_name, last_name);
CREATE INDEX idx_users_department ON users(department);