INSERT INTO user_group (id, external_id, name, description, date_created)
VALUES ( 1, uuid_to_bin(uuid()), 'Everyone', 'All the users', '2020-09-01' );

INSERT INTO user_groups (user_id, groups_id)
VALUES ( 1, 1 );