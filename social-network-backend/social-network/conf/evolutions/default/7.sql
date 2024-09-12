# --- !Ups
CREATE TABLE IF NOT EXISTS comments (
                                        id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                        post_id INT UNSIGNED NOT NULL,
                                        user_id INT UNSIGNED NOT NULL,
                                        content TEXT NOT NULL,
                                        parent_comment_id INT UNSIGNED DEFAULT NULL,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
                                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                        FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE
    );

-- Inserting test data
INSERT INTO comments (post_id, user_id, content) VALUES (1, 1, 'This is a comment by testuser1 on post 1');
INSERT INTO comments (post_id, user_id, content) VALUES (2, 2, 'This is a comment by testuser2 on post 2');
INSERT INTO comments (post_id, user_id, content, parent_comment_id) VALUES (1, 1, 'Reply to first comment by testuser1', 1);

# --- !Downs
DROP TABLE IF EXISTS comments;
