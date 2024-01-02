ALTER TABLE user_group
ADD COLUMN date_created datetime(6) DEFAULT NULL;

ALTER TABLE user_group
ADD COLUMN date_modified datetime(6) DEFAULT NULL;