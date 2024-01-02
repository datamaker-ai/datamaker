ALTER TABLE sink_configuration
ADD COLUMN date_created datetime(6) DEFAULT NULL;

ALTER TABLE sink_configuration
ADD COLUMN date_modified datetime(6) DEFAULT NULL;