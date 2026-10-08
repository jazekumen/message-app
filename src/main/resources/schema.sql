-- Database schema, created at startup by DatabaseConfiguration

CREATE TABLE IF NOT EXISTS users (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    username  VARCHAR(50)  NOT NULL UNIQUE,
    password  VARCHAR(100) NOT NULL,
    fullname  VARCHAR(100),
    email     VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS messages (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    text        VARCHAR(140) NOT NULL,
    location    VARCHAR(100),
    created_at  TIMESTAMP    NOT NULL,
    user_id     BIGINT       NOT NULL REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages (created_at);
