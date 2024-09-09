# --- !Ups
CREATE TABLE friendships (
                             id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                             user_id1 INT UNSIGNED NOT NULL,
                             user_id2 INT UNSIGNED NOT NULL,
                             FOREIGN KEY (user_id1) REFERENCES users(id) ON DELETE CASCADE,
                             FOREIGN KEY (user_id2) REFERENCES users(id) ON DELETE CASCADE
);

INSERT INTO friendships (user_id1, user_id2) VALUES (1, 2);

# --- !Downs
DROP TABLE IF EXISTS friendships;
