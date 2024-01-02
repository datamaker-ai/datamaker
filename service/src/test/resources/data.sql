INSERT INTO USER_GROUP (id, external_id, name, description) VALUES (1, 'd86f8326281d11ea978f2e728ce88121', 'test-all', ''), (2, 'd86f8326281d11ea978f2e728ce88122', 'test-nothing', ''), (3, 'd86f8326281d11ea978f2e728ce88123', 'test-readonly', '');

--INSERT INTO USER (id, external_id, first_name, last_name, password, authority, username, enabled, date_created) VALUES
--(1, 'd86f8326281d11ea978f2e728ce88125', 'admin', 'admin', '$2a$10$CLS9W5ogUXIwKY8RcM/TIOM0c0VECJaVC9qLi8.Yz7sB34z2BgCiG', 'ROLE_ADMIN', 'admin', true, '2020-01-01');

INSERT INTO USER (id, external_id, first_name, last_name, password, authority, username, date_created, enabled, locale) VALUES
(1, 'd86f8326281d11ea978f2e728ce88125', 'admin', 'admin', '$2a$10$CLS9W5ogUXIwKY8RcM/TIOM0c0VECJaVC9qLi8.Yz7sB34z2BgCiG', 'ROLE_ADMIN', 'admin', '2020-01-01', true, 'en-US'),
(2, 'd86f8326281d11ea978f2e728ce88121', 'Lokesh', 'Gupta', 'fewa78', 'ROLE_ADMIN', 'abc@gmail.com', '2008-08-08 08:08:08', true, 'en_CA'),
(3, 'd86f8592281d11ea978f2e728ce88122', 'Deja', 'Vu', 'chagneme', 'ROLE_USER', 'xyz@email.com', '2008-08-08 08:08:08', true, 'en_CA'),
(4, 'd86f86f0281d11ea978f2e728ce88123', 'Caption', 'America', 'feai70432^*', 'ROLE_USER', 'cap@marvel.com', '2008-08-08 08:08:08', true, 'en_CA'),
(5, 'd86f86f0281d11ea978f2e728ce88124', 'Rob', 'Winch', '12345', 'ROLE_USER', 'rob', '2008-08-08 08:08:08', true, 'en_CA');

INSERT INTO USER_GROUPS (user_id, groups_id) VALUES
(5, 3),
(4, 1);

INSERT INTO FIELD_MAPPING (id, external_id, mapping_key, field_json) VALUES
(null, 'd86f8326281d11ea978f2e728ce88125', 'test-fr', '{"name": "test", "languageTag": "fr", "className": "ai.datamaker.model.field.type.NameField"}'),
(null, 'd86f8326281d11ea978f2e728ce88126', 'test-en', '{"name": "test", "languageTag": "en", "className": "ai.datamaker.model.field.type.NameField"}'),
(null, 'd86f8326281d11ea978f2e728ce88127', 'update-en', '{"name": "update", "languageTag": "en", "className": "ai.datamaker.model.field.type.NameField"}'),
(null, random_uuid(), 'age-fr', '{"name": "Age", "description": "", "languageTag": "fr", "className": "ai.datamaker.model.field.type.AgeField" }'),
(null, random_uuid(), 'age-en', '{"name": "Age", "description": "", "languageTag": "en", "className": "ai.datamaker.model.field.type.AgeField" }'),
(null, random_uuid(), 'email-en', '{"name": "Email", "description": "", "languageTag": "en", "className": "ai.datamaker.model.field.type.EmailField" }'),
(null, random_uuid(), 'id-en', '{"name": "ID", "description": "", "languageTag": "en", "className": "ai.datamaker.model.field.type.SequenceField" }'),
(null, random_uuid(), 'fullname-en', '{"name": "Full name", "description": "", "languageTag": "en", "className": "ai.datamaker.model.field.type.NameField", "config": { "field.name.type": "FULL" } }'),
(null, random_uuid(), 'country-en', '{"name": "Full name", "description": "", "languageTag": "en", "className": "ai.datamaker.model.field.type.AddressField", "config": { "field.address.type": "COUNTRY" } }'),
(null, random_uuid(), 'cc-en', '{"name": "Full name", "description": "", "languageTag": "en", "className": "ai.datamaker.model.field.type.CreditCardField", "config": { } }'),
(null, random_uuid(), 'gender-en', '{"name": "Full name", "description": "", "languageTag": "en", "className": "ai.datamaker.model.field.type.DemographicField", "config": { "field.demographic.type": "GENDER" } }'),
(null, random_uuid(), 'ipaddress-en', '{"name": "Full name", "description": "", "languageTag": "en", "className": "ai.datamaker.model.field.type.NetworkField", "config": { } }'),
(null, random_uuid(), 'salary-en', '{"name": "Full name", "description": "", "languageTag": "en", "className": "ai.datamaker.model.field.type.MoneyField", "config": { } }'),
(null, random_uuid(), 'title-en', '{"name": "Full name", "description": "", "languageTag": "en", "className": "ai.datamaker.model.field.type.JobField", "config": { } }'),
(null, random_uuid(), 'name-en', '{"name": "Name", "description": "", "languageTag": "en", "className": "ai.datamaker.model.field.type.NameField", "config": { "field.name.type": "FULL" } }'),
(null, random_uuid(), 'nom-fr', '{"name": "Nom", "description": "", "languageTag": "fr", "className": "ai.datamaker.model.field.type.NameField", "config": { "field.name.type": "FULL" } }'),
(null, random_uuid(), 'prenom-fr', '{"name": "Prénom", "description": "", "languageTag": "fr", "className": "ai.datamaker.model.field.type.NameField", "config": { "field.name.type": "FIRST" } }'),
(null, random_uuid(), 'petitnom-fr-FR', '{"name": "Prénom", "description": "", "languageTag": "fr-FR", "className": "ai.datamaker.model.field.type.NameField", "config": { "field.name.type": "FIRST" } }'),
(null, random_uuid(), 'firstname-en', '{"name": "First name", "description": "", "languageTag": "en", "className": "ai.datamaker.model.field.type.NameField", "config": { "field.name.type": "FIRST" } }'),
(null, random_uuid(), 'lastname-en', '{"name": "Last name", "description": "", "languageTag": "en", "className": "ai.datamaker.model.field.type.NameField", "config": { "field.name.type": "LAST" } }'),
(null, random_uuid(), 'url-en', '{"name": "URL", "description": "", "languageTag": "en", "className": "ai.datamaker.model.field.type.UrlField", "config": { } }');

-- Insert workspace
INSERT INTO WORKSPACE (id, external_id, name, description, group_permissions, owner_id, user_group_id) VALUES
(1, 'd86f8326281d11ea978f2e728ce88127', 'my-workspace-full', 'full', 'FULL', 2, null),
(2, 'd86f8326281d11ea978f2e728ce88128', 'my-workspace-ro', 'ro', 'READ_ONLY', 2, 3),
(3, 'd86f8326281d11ea978f2e728ce88129', 'my-workspace-rw', 'rw', 'READ_WRITE', 4, null),
(4, 'd86f8326281d11ea978f2e728ce88130', 'my-workspace-none', 'none', 'NONE', 5, null),
(5, 'd86f8326281d11ea978f2e728ce88131', 'my-workspace-datasets', 'datasets', 'NONE', 2, null),
(6, 'd86f8326281d11ea978f2e728ce88132', 'change-owner', 'rw', 'READ_WRITE', 2, 3);

INSERT INTO DATASET (id, external_id, name, description, workspace_id) VALUES
(1, 'd86f8326281d11ea978f2e728ce88127', 'my-dataset-from-full', 'full-workspace', 1),
(2, 'd86f8326281d11ea978f2e728ce88128', 'my-dataset-from-ro', 'ro-workspace', 2);

INSERT INTO FIELD (id, dtype, external_id, locale, name, is_nullable, is_primary_key, is_attribute, dataset_id, position) VALUES
    (null, 'StringField', 'd86f8326281d11ea978f2e728ce88125', 'en_CA', 'junit', true, false, false, 1, 1),
    (null, 'NameField', 'd86f8326281d11ea978f2e728ce88126', 'en_CA', 'my-name', true, false, false, 1, 1);

--INSERT INTO GENERATE_DATA_JOB (id, external_id, name, description, workspace_id, config) VALUES
--(1, 'd86f8326281d11ea978f2e728ce88127', 'datajob-json', 'Datajob JSON', 1, null),
--(2, 'd86f8326281d11ea978f2e728ce88128', 'datajob-csv', 'Datajob CSV', 2, null),
--(3, 'd86f8326281d11ea978f2e728ce88129', 'datajob-xml', 'Datajob XML', 2, 'aced00057372002863612e627265616b706f696e74732e646174616d616b65722e6d6f64656c2e4a6f62436f6e666967df43bb1b0dae6ee1020000787200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f4000000000000c7708000000100000000274003163612e627265616b706f696e74732e646174616d616b65722e73696e6b2e626173652e46696c654f757470757453696e6b7371007e00003f4000000000000c7708000000100000000174000b646174617365744e616d6574000d57415443484c4953542e6373767874003063612e627265616b706f696e74732e646174616d616b65722e67656e657261746f722e4a736f6e47656e657261746f727371007e00003f40000000000000770800000010000000007878');

