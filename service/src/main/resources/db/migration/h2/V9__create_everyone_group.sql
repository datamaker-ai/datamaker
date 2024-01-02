INSERT INTO USER_GROUP (id, external_id, name, description, date_created)
VALUES ( 1, random_uuid(), 'Everyone', 'All the users', '2020-09-01' );

INSERT INTO USER_GROUPS (USER_ID, GROUPS_ID)
VALUES ( 1, 1 );