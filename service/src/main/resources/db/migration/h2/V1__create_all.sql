create table dataset (id bigint generated by default as identity, allow_duplicates boolean, date_created timestamp, date_modified timestamp, description varchar(255), duplicates_percent_limit float, export_header boolean, external_id uuid not null, locale varchar(255), name varchar(255), nullable_percent_limit float, number_of_records bigint, number_of_retries integer, thread_pool_size integer, workspace_id bigint, primary key (id));

create table dataset_tags (dataset_id bigint not null, tags varchar(255));

create table field (dtype varchar(31) not null, id bigint generated by default as identity, config blob, description varchar(255), external_id uuid not null, formatter_class_name varchar(255), is_alias boolean, is_attribute boolean, is_nullable boolean not null, is_primary_key boolean, is_nested boolean, locale varchar(255) not null, name varchar(255), null_value varchar(255), position integer, type integer, dataset_id bigint, date_created timestamp, date_modified timestamp, primary key (id));

create table field_mapping (id bigint generated by default as identity, external_id uuid not null, field_json varchar(10000), mapping_key varchar(255), locale varchar(255), primary key (id));

create table generate_data_job (id bigint generated by default as identity, buffer_size integer, config blob, description varchar(255), external_id uuid not null, generator_name varchar(255), name varchar(255), number_of_records bigint, run_status boolean, schedule varchar(255), size bigint, stream_forever boolean, use_buffer boolean default false not null, workspace_id bigint, primary key (id), date_created timestamp, date_modified timestamp, randomize_number_of_records boolean default false not null, flush_on_every_record boolean default true not null);

create table generate_data_job_dataset (generate_data_job_id bigint not null, dataset_id bigint not null);

create table generate_data_job_sink_names (generate_data_job_id bigint not null, sink_names varchar(255));

create table job_execution (id bigint generated by default as identity, number_of_records bigint, cancel_time timestamp, end_time timestamp, external_id uuid not null, is_cancelled boolean, is_success boolean, start_time timestamp, state varchar(25), data_job_id bigint, primary key (id));

create table job_execution_errors (job_execution_id bigint not null, errors varchar(2048));

create table job_execution_results (job_execution_id bigint not null, results varchar(1024));

create table sink_configuration (id bigint generated by default as identity, external_id uuid not null, job_config blob, name varchar(255) not null, sink_class_name varchar(255), workspace_id bigint, primary key (id));

create table user (id bigint generated by default as identity, authority varchar(255) not null, date_created timestamp, date_modified timestamp, enabled boolean, external_id uuid not null, first_name varchar(255), last_name varchar(255), locale varchar(255), password varchar(255), username varchar(255) not null, primary key (id));

create table user_groups (user_id bigint not null, groups_id bigint not null);

create table user_group (id bigint generated by default as identity, description varchar(255), external_id uuid not null, name varchar(255) not null, primary key (id));

create table workspace (id bigint generated by default as identity, date_created timestamp, date_modified timestamp, description varchar(255), external_id uuid not null, group_permissions varchar(255), name varchar(255) not null, owner_id bigint, user_group_id bigint, primary key (id));

alter table dataset drop constraint if exists UK_74lq42mhcy2xoirqeiv0eb591;

alter table dataset add constraint UK_74lq42mhcy2xoirqeiv0eb591 unique (external_id);

alter table field drop constraint if exists UK_lcdf6cku9nq0colrc44a7fsxb;

alter table field add constraint UK_lcdf6cku9nq0colrc44a7fsxb unique (external_id);

create index IDX3naijo6prvr0nh6t34dq4m3em on field_mapping (mapping_key);

alter table field_mapping drop constraint if exists UK_4mqyvvvn868mkkdypv7ohourl;

alter table field_mapping add constraint UK_4mqyvvvn868mkkdypv7ohourl unique (external_id);

alter table field_mapping drop constraint if exists UK_3naijo6prvr0nh6t34dq4m3em;

alter table field_mapping add constraint UK_3naijo6prvr0nh6t34dq4m3em unique (mapping_key);

alter table generate_data_job drop constraint if exists UK_jni7kj63h4cadqkohxm2eeevx;

alter table generate_data_job add constraint UK_jni7kj63h4cadqkohxm2eeevx unique (external_id);

alter table job_execution drop constraint if exists UK_7ftcwbbuul2jqka0w7790wm70;

alter table job_execution add constraint UK_7ftcwbbuul2jqka0w7790wm70 unique (external_id);

alter table sink_configuration drop constraint if exists UK_3bs71skilii07h1rmrqi9mh2o;

alter table sink_configuration add constraint UK_3bs71skilii07h1rmrqi9mh2o unique (external_id);

alter table sink_configuration drop constraint if exists UK_fksxsb9y2nef77j7wsy8d63u7;

alter table sink_configuration add constraint UK_fksxsb9y2nef77j7wsy8d63u7 unique (name);

alter table user drop constraint if exists UK_4eu2tvn9rj53a93fufx7ayr20;

alter table user add constraint UK_4eu2tvn9rj53a93fufx7ayr20 unique (external_id);

alter table user drop constraint if exists UK_sb8bbouer5wak8vyiiy4pf2bx;

alter table user add constraint UK_sb8bbouer5wak8vyiiy4pf2bx unique (username);

alter table user_group drop constraint if exists UK_oid76j4848jmwaqf1e8qhi0y2;

alter table user_group add constraint UK_oid76j4848jmwaqf1e8qhi0y2 unique (external_id);

alter table user_group drop constraint if exists UK_kas9w8ead0ska5n3csefp2bpp;

alter table user_group add constraint UK_kas9w8ead0ska5n3csefp2bpp unique (name);

alter table workspace drop constraint if exists UK_5r69hlylku2sqe2v6qn5l1y71;

alter table workspace add constraint UK_5r69hlylku2sqe2v6qn5l1y71 unique (external_id);

alter table workspace drop constraint if exists UK_br8l0q43h1ygdohbp4htocj3h;

alter table workspace add constraint UK_br8l0q43h1ygdohbp4htocj3h unique (name);

create sequence hibernate_sequence start with 1 increment by 1;

alter table dataset add constraint FKqckm5n91r0j83x60ue6rt0lu4 foreign key (workspace_id) references workspace;

alter table dataset_tags add constraint FKd1tbt6wpahgstthwqcifop9n7 foreign key (dataset_id) references dataset;

alter table field add constraint FK5oh59oq1hdqjk32mu5i435pnp foreign key (dataset_id) references dataset;

alter table generate_data_job add constraint FK49rl95cstbwc8cqk931pvo8yc foreign key (workspace_id) references workspace;

alter table generate_data_job_dataset add constraint FKelfp4ivx1fm4dtmmhabl9650u foreign key (dataset_id) references dataset;

alter table generate_data_job_dataset add constraint FK9fpnh55ma0q7620a8fmvmxf3g foreign key (generate_data_job_id) references generate_data_job;

alter table generate_data_job_sink_names add constraint IF NOT EXISTS FKg0qipuoy1orytw1hfrxhnuipy foreign key (generate_data_job_id) references generate_data_job;

alter table job_execution add constraint FK46ap65w8ypndrdoe4o7gtd55 foreign key (data_job_id) references generate_data_job;

alter table job_execution_errors add constraint FKiyy53uxmwvykj8qp1coytoyav foreign key (job_execution_id) references job_execution;

alter table job_execution_results add constraint FKhdqo0fcbheg0odrph0edgos5u foreign key (job_execution_id) references job_execution;

alter table sink_configuration add constraint FKnrafxsw3j4iyixuvfuchjl2o3 foreign key (workspace_id) references workspace;

alter table user_groups add constraint FK2nhhy7f9gn8ay6bl34e9kex95 foreign key (groups_id) references user_group;

alter table user_groups add constraint FKxgk67l5yp8458l39rog6nppe foreign key (user_id) references user;

alter table workspace add constraint FKk7dgp9sb1paeoxv6iudh1snt5 foreign key (owner_id) references user;

alter table workspace add constraint FK4gwkebjndygjcc2q1ekn1hjwk foreign key (user_group_id) references user_group;



    