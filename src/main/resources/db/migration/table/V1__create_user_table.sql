CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT                   NOT NULL,
    created_at datetime                                NOT NULL,
    updated_at datetime                                NOT NULL,
    email      VARCHAR(150) COLLATE utf8mb4_unicode_ci NOT NULL,
    password   VARCHAR(100)                            NOT NULL,
    status     VARCHAR(10)                             NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);