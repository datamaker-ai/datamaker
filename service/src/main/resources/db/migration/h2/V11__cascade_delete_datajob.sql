alter table job_execution drop constraint FK46ap65w8ypndrdoe4o7gtd55;
alter table job_execution add constraint FKs1k6a3rkwvo1psqe51y14gadq foreign key (data_job_id) references generate_data_job on delete cascade;

alter table job_execution_errors drop constraint FKiyy53uxmwvykj8qp1coytoyav;
alter table job_execution_results drop constraint FKhdqo0fcbheg0odrph0edgos5u;
alter table job_execution_errors add constraint FKiyy53uxmwvykj8qp1coytoyva foreign key (job_execution_id) references job_execution on delete cascade;
alter table job_execution_results add constraint FKhdqo0fcbheg0odrph0edgosu5 foreign key (job_execution_id) references job_execution on delete cascade;
