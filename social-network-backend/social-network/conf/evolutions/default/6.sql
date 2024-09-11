# --- !Ups
ALTER TABLE users ADD COLUMN profile_photo VARCHAR(255) DEFAULT '/assets/images/default.png';

# --- !Downs
ALTER TABLE users DROP COLUMN profile_photo;
