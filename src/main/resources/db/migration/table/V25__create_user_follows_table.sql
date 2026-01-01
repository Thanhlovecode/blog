CREATE TABLE user_follows
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    created_at   datetime              NOT NULL,
    updated_at   datetime              NOT NULL,
    follower_id  BIGINT                NOT NULL,
    following_id BIGINT                NOT NULL,
    CONSTRAINT pk_user_follows PRIMARY KEY (id)
);

ALTER TABLE user_follows
    ADD CONSTRAINT uc_6d853ba591318892d304aa007 UNIQUE (follower_id, following_id);

ALTER TABLE user_follows
    ADD CONSTRAINT FK_USER_FOLLOWS_ON_FOLLOWER FOREIGN KEY (follower_id) REFERENCES users (id);

ALTER TABLE user_follows
    ADD CONSTRAINT FK_USER_FOLLOWS_ON_FOLLOWING FOREIGN KEY (following_id) REFERENCES users (id);

CREATE INDEX idx_following ON user_follows (following_id);