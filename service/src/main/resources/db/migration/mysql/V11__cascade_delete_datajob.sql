ALTER TABLE job_execution DROP FOREIGN KEY FKs1k6a3rkwvo1psqe51y14gadq;
ALTER TABLE job_execution ADD CONSTRAINT FKs1k6a3rkwvo1psqe51y14gadq FOREIGN KEY (`data_job_id`) REFERENCES `generate_data_job` (`id`) ON DELETE CASCADE;