CREATE TABLE post_tag
(
    post_id BIGINT NOT NULL,
    tag_id  BIGINT NOT NULL,
    CONSTRAINT pk_post_tag PRIMARY KEY (post_id, tag_id)
);

CREATE TABLE posts
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    created_at     datetime              NOT NULL,
    updated_at     datetime              NOT NULL,
    title          VARCHAR(255)          NOT NULL,
    slug           VARCHAR(255)          NOT NULL,
    excerpt        VARCHAR(500)          NULL,
    username       VARCHAR(100)          NULL,
    reading_time   INT                   NOT NULL,
    thumbnail_url  VARCHAR(300)          NULL,
    total_comments INT default 0                NOT NULL,
    total_views    INT default 0                NOT NULL,
    total_likes    INT default 0                 NOT NULL,
    status         VARCHAR(20)          NOT NULL,
    user_id        BIGINT                NULL,
    published_at   datetime              NOT NULL,
    CONSTRAINT pk_posts PRIMARY KEY (id)
);

ALTER TABLE posts
    ADD CONSTRAINT uc_posts_slug UNIQUE (slug);

ALTER TABLE posts
    ADD CONSTRAINT FK_POSTS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE post_tag
    ADD CONSTRAINT fk_post_tag_on_post FOREIGN KEY (post_id) REFERENCES posts (id);

ALTER TABLE post_tag
    ADD CONSTRAINT fk_post_tag_on_tag FOREIGN KEY (tag_id) REFERENCES tag (id);