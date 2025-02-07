CREATE TABLE IF NOT EXISTS reward (
    id BIGSERIAL PRIMARY KEY,
    event_name VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    total_quantity INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    limit_from_date DATE,
    limit_to_date DATE,
    max_quantity_per_period INTEGER,
    applicable_provinces TEXT[] NOT NULL,
    image_url VARCHAR(255)
);

-- Thêm các bảng khác nếu cần
