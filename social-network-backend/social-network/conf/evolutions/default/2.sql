# --- !Ups
CREATE TABLE IF NOT EXISTS posts (
                       id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                       user_id INT UNSIGNED NOT NULL,
                       content TEXT NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT INTO posts (user_id, content) VALUES (1, 'This is the first post by testuser1');
INSERT INTO posts (user_id, content) VALUES (2, 'This is the first post by testuser2');
INSERT INTO posts (user_id, content) VALUES (3, 'This is the first post by existinguser');


# --- !Downs
DROP TABLE IF EXISTS posts;
