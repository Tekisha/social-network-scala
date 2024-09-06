# --- !Ups
CREATE TABLE friend_requests (
                                 id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                 requester_id INT UNSIGNED NOT NULL,
                                 receiver_id INT UNSIGNED NOT NULL,
                                 status ENUM('pending', 'accepted', 'rejected') DEFAULT 'pending',
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
                                 FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
);

INSERT INTO friend_requests (requester_id, receiver_id, status) VALUES (1, 2, 'accepted');
INSERT INTO friend_requests (requester_id, receiver_id, status) VALUES (2, 3, 'pending');
INSERT INTO friend_requests (requester_id, receiver_id, status) VALUES (3, 1, 'rejected');

# --- !Downs
DROP TABLE IF EXISTS friend_requests;
