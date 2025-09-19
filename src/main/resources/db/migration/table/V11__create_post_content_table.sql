CREATE TABLE post_content
(
    post_id BIGINT   NOT NULL,
    content LONGTEXT NOT NULL,
    CONSTRAINT pk_post_content PRIMARY KEY (post_id)
);

ALTER TABLE post_content
    ADD CONSTRAINT FK_POST_CONTENT_ON_POST FOREIGN KEY (post_id) REFERENCES posts (id);