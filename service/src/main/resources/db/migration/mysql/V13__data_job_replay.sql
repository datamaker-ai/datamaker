ALTER TABLE job_execution
    ADD COLUMN replay bit(1) DEFAULT NULL;

ALTER TABLE generate_data_job
    ADD COLUMN replayable bit(1) DEFAULT NULL;

ALTER TABLE generate_data_job
    ADD COLUMN replay_history_size int DEFAULT 10;
