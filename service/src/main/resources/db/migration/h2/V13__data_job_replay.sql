ALTER TABLE job_execution
    ADD COLUMN replay boolean DEFAULT false;

ALTER TABLE generate_data_job
    ADD COLUMN replayable boolean DEFAULT false;

ALTER TABLE generate_data_job
    ADD COLUMN replay_history_size integer DEFAULT 10;
