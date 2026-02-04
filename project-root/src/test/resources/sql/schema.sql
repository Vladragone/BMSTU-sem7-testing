CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    score INTEGER NOT NULL DEFAULT 0,
    reg_date TIMESTAMP NOT NULL,
    game_num INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_profile_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS location_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS game_session (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    location_group_id BIGINT NOT NULL,
    total_score INTEGER NOT NULL DEFAULT 0,
    total_rounds INTEGER NOT NULL,
    CONSTRAINT fk_game_session_location_group
        FOREIGN KEY (location_group_id)
        REFERENCES location_groups (id)
        ON DELETE RESTRICT
);
