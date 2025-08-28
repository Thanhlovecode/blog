CREATE TABLE tag
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_at    datetime              NOT NULL,
    updated_at    datetime              NOT NULL,
    name          VARCHAR(150)          NOT NULL,
    thumbnail_url VARCHAR(255)          NULL,
    slug          VARCHAR(150)          NOT NULL,
    CONSTRAINT pk_tag PRIMARY KEY (id)
);

ALTER TABLE tag
    ADD CONSTRAINT uc_tag_slug UNIQUE (slug);