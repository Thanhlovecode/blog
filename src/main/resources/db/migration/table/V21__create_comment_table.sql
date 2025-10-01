CREATE TABLE comments
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    created_at        datetime              NOT NULL,
    updated_at        datetime              NOT NULL,
    content           VARCHAR(2000)         NOT NULL,
    username          VARCHAR(100)          NOT NULL,
    display_name      VARCHAR(100)          NOT NULL,
    user_avatar       VARCHAR(300)          NULL,
    total_likes       INT default 0         NOT NULL,
    user_id           BIGINT                NOT NULL,
    post_id           BIGINT                NOT NULL,
    parent_comment_id BIGINT                NULL,
    is_deleted        BIT(1)                NOT NULL,
    CONSTRAINT pk_comments PRIMARY KEY (id)
);

ALTER TABLE comments
    ADD CONSTRAINT FK_COMMENTS_ON_POST FOREIGN KEY (post_id) REFERENCES posts (id);

ALTER TABLE comments
    ADD CONSTRAINT FK_COMMENTS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);