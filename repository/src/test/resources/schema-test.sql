-- Drop tables if they exist
DROP TABLE IF EXISTS event_locations;
DROP TABLE IF EXISTS provinces;
DROP TABLE IF EXISTS regions;

-- Create regions table
CREATE TABLE regions (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    default_win_probability DOUBLE PRECISION
);

-- Create provinces table
CREATE TABLE provinces (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    default_win_probability DOUBLE PRECISION,
    region_id BIGINT REFERENCES regions(id)
);

-- Create event_locations table
CREATE TABLE event_locations (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    status INTEGER NOT NULL DEFAULT 1,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    region_id BIGINT REFERENCES regions(id)
);
