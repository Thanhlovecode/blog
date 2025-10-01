CREATE INDEX idx_posts_user_status_published_id
    ON posts(username, status, published_at DESC, id DESC);