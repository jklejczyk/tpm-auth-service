CREATE TABLE users
(
    id            VARCHAR(64) PRIMARY KEY,
    username      VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(72) NOT NULL,
    role          VARCHAR(32) NOT NULL
);
