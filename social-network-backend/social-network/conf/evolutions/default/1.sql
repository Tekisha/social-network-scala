# --- !Ups
CREATE TABLE IF NOT EXISTS users (
                       id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL
);

INSERT INTO users (username, password) VALUES ('testuser1', '$2a$10$Dp47YK6M37TETlFKIwz.s.qQxH0A51LTgGgdKQUtPt6fIyBLqiv36');
INSERT INTO users (username, password) VALUES ('testuser2', '$2a$10$BPeIc3jSy7w7CCvS5TJAk.IHvSfJpBS5cL.TQALIcOxqnC6hFFefC');
INSERT INTO users (username, password) VALUES ('existinguser', '$2a$10$AcdZ4Lag6JERRxw3YA5J6OWZDooT2UFojgko1uB8qcRFKY4ZVKqUu');

# --- !Downs
DROP TABLE IF EXISTS users;


