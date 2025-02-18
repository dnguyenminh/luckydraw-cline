-- Drop tables if they exist
DROP TABLE IF EXISTS spin_histories CASCADE;
DROP TABLE IF EXISTS lucky_draw_results CASCADE;
DROP TABLE IF EXISTS golden_hours CASCADE;
DROP TABLE IF EXISTS rewards CASCADE;
DROP TABLE IF EXISTS participants CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create tables
CREATE TABLE users
(
    id               BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username         VARCHAR(255) NOT NULL UNIQUE,
    email            VARCHAR(255) UNIQUE,
    password         VARCHAR(255) NOT NULL,
    first_name       VARCHAR(255),
    last_name        VARCHAR(255),
    phone_number     VARCHAR(50),
    address          TEXT,
    enabled          BOOLEAN      NOT NULL DEFAULT true,
    status           VARCHAR(50),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(255),
    last_modified_by VARCHAR(255),
    version          BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE roles
(
    id               BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name             VARCHAR(50) NOT NULL UNIQUE,
    description      VARCHAR(255),
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(255),
    last_modified_by VARCHAR(255),
    version          BIGINT      NOT NULL DEFAULT 0
);

CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE events
(
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    code            VARCHAR(255) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    start_date      TIMESTAMP    NOT NULL,
    end_date        TIMESTAMP    NOT NULL,
    total_spins     BIGINT       NOT NULL DEFAULT 0,
    remaining_spins BIGINT,
    is_active       BOOLEAN               DEFAULT true,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         BIGINT                DEFAULT 0,
    CONSTRAINT chk_event_dates CHECK (end_date > start_date)
);

CREATE TABLE participants
(
    id               BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    event_id         BIGINT       NOT NULL,
    name             VARCHAR(255) NOT NULL,
    employee_id      VARCHAR(50),
    full_name        VARCHAR(255),
    is_active        BOOLEAN               DEFAULT true,
    department       VARCHAR(255),
    position         VARCHAR(255),
    email            VARCHAR(255),
    phone_number     VARCHAR(50),
    province         VARCHAR(255),
    customer_id      VARCHAR(50),
    card_number      VARCHAR(50),
    daily_spin_limit INT                   DEFAULT 0,
    spins_remaining  INT                   DEFAULT 0,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version          BIGINT                DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES events (id)
);

CREATE TABLE rewards
(
    id                     BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    event_id               BIGINT       NOT NULL,
    name                   VARCHAR(255) NOT NULL,
    description            TEXT,
    quantity               INTEGER      NOT NULL,
    remaining_quantity     INTEGER      NOT NULL,
    max_quantity_in_period INTEGER,
    probability            DOUBLE PRECISION,
    applicable_provinces   VARCHAR(1000),
    start_date             TIMESTAMP,
    end_date               TIMESTAMP,
    is_active              BOOLEAN      NOT NULL DEFAULT true,
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                BIGINT                DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT chk_reward_quantity CHECK (remaining_quantity >= 0 AND remaining_quantity <= quantity)
);

CREATE TABLE golden_hours
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    event_id   BIGINT    NOT NULL,
    name       VARCHAR(255),
    start_time TIMESTAMP NOT NULL,
    end_time   TIMESTAMP NOT NULL,
    reward_id  BIGINT,
    multiplier DOUBLE PRECISION   DEFAULT 1.0,
    is_active  BOOLEAN            DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version    BIGINT             DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES events (id),
    FOREIGN KEY (reward_id) REFERENCES rewards (id)
);

CREATE TABLE spin_histories
(
    id                 BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    participant_id     BIGINT    NOT NULL,
    event_id           BIGINT    NOT NULL,
    reward_id          BIGINT,
    spin_time          TIMESTAMP NOT NULL,
    result             TEXT,
    won                BOOLEAN            DEFAULT false,
    is_golden_hour     BOOLEAN            DEFAULT false,
    current_multiplier DOUBLE PRECISION   DEFAULT 1.0,
    remaining_spins    INT,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version            BIGINT             DEFAULT 0,
    FOREIGN KEY (participant_id) REFERENCES participants (id),
    FOREIGN KEY (event_id) REFERENCES events (id),
    FOREIGN KEY (reward_id) REFERENCES rewards (id)
);

CREATE TABLE lucky_draw_results
(
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    event_id        BIGINT    NOT NULL,
    participant_id  BIGINT    NOT NULL,
    reward_id       BIGINT    NOT NULL,
    spin_history_id BIGINT    NOT NULL,
    win_time        TIMESTAMP NOT NULL,
    pack_number     INTEGER,
    is_claimed      BOOLEAN   DEFAULT FALSE,
    claimed_at      TIMESTAMP,
    claimed_by      VARCHAR(255),
    claim_notes     TEXT,
    version         BIGINT    DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events (id),
    FOREIGN KEY (participant_id) REFERENCES participants (id),
    FOREIGN KEY (reward_id) REFERENCES rewards (id),
    FOREIGN KEY (spin_history_id) REFERENCES spin_histories (id)
);

-- Create indexes
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_enabled ON users (enabled);
CREATE INDEX idx_events_dates ON events (start_date, end_date);
CREATE INDEX idx_rewards_dates ON rewards (start_date, end_date);
CREATE INDEX idx_golden_hours_time ON golden_hours (start_time, end_time);
CREATE INDEX idx_participants_event_id ON participants (event_id);
CREATE INDEX idx_participants_employee_id ON participants (employee_id);
CREATE INDEX idx_participants_customer_id ON participants (customer_id);
CREATE INDEX idx_spin_histories_event_id ON spin_histories (event_id);
CREATE INDEX idx_spin_histories_participant_id ON spin_histories (participant_id);
CREATE INDEX idx_spin_histories_spin_time ON spin_histories (spin_time);
CREATE INDEX idx_lucky_draw_results_reward ON lucky_draw_results (reward_id);
CREATE INDEX idx_lucky_draw_results_participant ON lucky_draw_results (participant_id);
CREATE INDEX idx_lucky_draw_results_claimed ON lucky_draw_results (is_claimed);