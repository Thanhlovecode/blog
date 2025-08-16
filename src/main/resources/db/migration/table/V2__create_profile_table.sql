CREATE TABLE profiles
(
    id         BIGINT       NOT NULL,
    first_name VARCHAR(50) NULL,
    last_name  VARCHAR(50) NULL,
    avatar     VARCHAR(255) NULL,
    address    VARCHAR(255) NULL,
    birthday   date         NULL,
    phone      VARCHAR(20) NULL,
    gender     VARCHAR(10) NULL,
    CONSTRAINT pk_profiles PRIMARY KEY (id)
);

ALTER TABLE profiles
    ADD CONSTRAINT FK_PROFILES_ON_ID FOREIGN KEY (id) REFERENCES users (id);