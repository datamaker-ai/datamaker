-- Adminer 4.7.6 MySQL dump

SET NAMES utf8;
SET time_zone = '+00:00';
SET foreign_key_checks = 0;
SET sql_mode = 'NO_AUTO_VALUE_ON_ZERO';

SET NAMES utf8mb4;

DROP TABLE IF EXISTS `dataset`;
CREATE TABLE `dataset` (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `allow_duplicates` bit(1) DEFAULT NULL,
                           `date_created` datetime(6) DEFAULT NULL,
                           `date_modified` datetime(6) DEFAULT NULL,
                           `description` varchar(255) DEFAULT NULL,
                           `duplicates_percent_limit` float DEFAULT NULL,
                           `export_header` bit(1) DEFAULT NULL,
                           `external_id` binary(16) NOT NULL,
                           `locale` varchar(255) DEFAULT NULL,
                           `name` varchar(255) DEFAULT NULL,
                           `nullable_percent_limit` float DEFAULT NULL,
                           `number_of_records` bigint DEFAULT NULL,
                           `number_of_retries` int DEFAULT NULL,
                           `thread_pool_size` int DEFAULT NULL,
                           `workspace_id` bigint DEFAULT NULL,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `UK_74lq42mhcy2xoirqeiv0eb591` (`external_id`),
                           KEY `FKqckm5n91r0j83x60ue6rt0lu4` (`workspace_id`),
                           CONSTRAINT `FKqckm5n91r0j83x60ue6rt0lu4` FOREIGN KEY (`workspace_id`) REFERENCES `workspace` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `dataset_tags`;
CREATE TABLE `dataset_tags` (
                                `dataset_id` bigint NOT NULL,
                                `tags` varchar(255) DEFAULT NULL,
                                KEY `FKd1tbt6wpahgstthwqcifop9n7` (`dataset_id`),
                                CONSTRAINT `FKd1tbt6wpahgstthwqcifop9n7` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `field`;
CREATE TABLE `field` (
                         `dtype` varchar(31) NOT NULL,
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `config` longblob,
                         `description` varchar(255) DEFAULT NULL,
                         `external_id` binary(16) NOT NULL,
                         `formatter_class_name` varchar(255) DEFAULT NULL,
                         `is_alias` bit(1) DEFAULT NULL,
                         `is_attribute` bit(1) DEFAULT NULL,
                         `is_nested` bit(1) DEFAULT NULL,
                         `is_nullable` bit(1) DEFAULT NULL,
                         `is_primary_key` bit(1) DEFAULT NULL,
                         `locale` varchar(255) NOT NULL,
                         `name` varchar(255) DEFAULT NULL,
                         `null_value` varchar(255) DEFAULT NULL,
                         `position` int DEFAULT NULL,
                         `type` int DEFAULT NULL,
                         `dataset_id` bigint DEFAULT NULL,
                         `date_created` datetime(6) DEFAULT NULL,
                         `date_modified` datetime(6) DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `UK_lcdf6cku9nq0colrc44a7fsxb` (`external_id`),
                         KEY `FK5oh59oq1hdqjk32mu5i435pnp` (`dataset_id`),
                         CONSTRAINT `FK5oh59oq1hdqjk32mu5i435pnp` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `field_mapping`;
CREATE TABLE `field_mapping` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `external_id` binary(16) NOT NULL,
                                 `field_json` varchar(10000) DEFAULT NULL,
                                 `mapping_key` varchar(255) DEFAULT NULL,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `UK_4mqyvvvn868mkkdypv7ohourl` (`external_id`),
                                 UNIQUE KEY `UK_28j2lusfehfccl9ahvbbwpo7j` (`mapping_key`),
                                 KEY `IDXkxhxdxikh79bbjdt8byokyxwo` (`mapping_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `generate_data_job`;
CREATE TABLE `generate_data_job` (
                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                     `buffer_size` int DEFAULT NULL,
                                     `config` longblob,
                                     `description` varchar(255) DEFAULT NULL,
                                     `external_id` binary(16) NOT NULL,
                                     `generator_name` varchar(255) DEFAULT NULL,
                                     `name` varchar(255) DEFAULT NULL,
                                     `number_of_records` bigint DEFAULT NULL,
                                     `run_status` bit(1) DEFAULT NULL,
                                     `schedule` varchar(255) DEFAULT NULL,
                                     `size` bigint DEFAULT NULL,
                                     `stream_forever` bit(1) DEFAULT NULL,
                                     `use_buffer` bit(1) DEFAULT NULL,
                                     `workspace_id` bigint DEFAULT NULL,
                                     `date_created` datetime(6) DEFAULT NULL,
                                     `date_modified` datetime(6) DEFAULT NULL,
                                     `randomize_number_of_records` bit(1) DEFAULT NULL,
                                     `flush_on_every_record` bit(1) DEFAULT NULL,
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `UK_jni7kj63h4cadqkohxm2eeevx` (`external_id`),
                                     KEY `FK49rl95cstbwc8cqk931pvo8yc` (`workspace_id`),
                                     CONSTRAINT `FK49rl95cstbwc8cqk931pvo8yc` FOREIGN KEY (`workspace_id`) REFERENCES `workspace` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `generate_data_job_dataset`;
CREATE TABLE `generate_data_job_dataset` (
                                             `generate_data_job_id` bigint NOT NULL,
                                             `dataset_id` bigint NOT NULL,
                                             KEY `FKelfp4ivx1fm4dtmmhabl9650u` (`dataset_id`),
                                             KEY `FK9fpnh55ma0q7620a8fmvmxf3g` (`generate_data_job_id`),
                                             CONSTRAINT `FK9fpnh55ma0q7620a8fmvmxf3g` FOREIGN KEY (`generate_data_job_id`) REFERENCES `generate_data_job` (`id`),
                                             CONSTRAINT `FKelfp4ivx1fm4dtmmhabl9650u` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `generate_data_job_sink_names`;
CREATE TABLE `generate_data_job_sink_names` (
                                                `generate_data_job_id` bigint NOT NULL,
                                                `sink_names` varchar(255) DEFAULT NULL,
                                                KEY `FKj3oqpdg4lxarfj8xatisiylg5` (`generate_data_job_id`),
                                                CONSTRAINT `FKj3oqpdg4lxarfj8xatisiylg5` FOREIGN KEY (`generate_data_job_id`) REFERENCES `generate_data_job` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `hibernate_sequence`;
CREATE TABLE `hibernate_sequence` (
    `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `job_execution`;
CREATE TABLE `job_execution` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `number_of_records` bigint DEFAULT NULL,
                                 `cancel_time` datetime(6) DEFAULT NULL,
                                 `end_time` datetime(6) DEFAULT NULL,
                                 `external_id` binary(16) NOT NULL,
                                 `is_success` bit(1) DEFAULT NULL,
                                 `start_time` datetime(6) DEFAULT NULL,
                                 `state` varchar(255) DEFAULT NULL,
                                 `data_job_id` bigint DEFAULT NULL,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `UK_7ftcwbbuul2jqka0w7790wm70` (`external_id`),
                                 KEY `FKs1k6a3rkwvo1psqe51y14gadq` (`data_job_id`),
                                 CONSTRAINT `FKs1k6a3rkwvo1psqe51y14gadq` FOREIGN KEY (`data_job_id`) REFERENCES `generate_data_job` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `job_execution_errors`;
CREATE TABLE `job_execution_errors` (
                                        `job_execution_id` bigint NOT NULL,
                                        `errors` varchar(255) DEFAULT NULL,
                                        KEY `FKiyy53uxmwvykj8qp1coytoyav` (`job_execution_id`),
                                        CONSTRAINT `FKiyy53uxmwvykj8qp1coytoyav` FOREIGN KEY (`job_execution_id`) REFERENCES `job_execution` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `job_execution_results`;
CREATE TABLE `job_execution_results` (
                                         `job_execution_id` bigint NOT NULL,
                                         `results` varchar(255) DEFAULT NULL,
                                         KEY `FKhdqo0fcbheg0odrph0edgos5u` (`job_execution_id`),
                                         CONSTRAINT `FKhdqo0fcbheg0odrph0edgos5u` FOREIGN KEY (`job_execution_id`) REFERENCES `job_execution` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `sink_configuration`;
CREATE TABLE `sink_configuration` (
                                      `id` bigint NOT NULL AUTO_INCREMENT,
                                      `external_id` binary(16) NOT NULL,
                                      `job_config` longblob,
                                      `name` varchar(255) NOT NULL,
                                      `sink_class_name` varchar(255) DEFAULT NULL,
                                      `workspace_id` bigint DEFAULT NULL,
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `UK_3bs71skilii07h1rmrqi9mh2o` (`external_id`),
                                      UNIQUE KEY `UK_fksxsb9y2nef77j7wsy8d63u7` (`name`),
                                      KEY `FKnrafxsw3j4iyixuvfuchjl2o3` (`workspace_id`),
                                      CONSTRAINT `FKnrafxsw3j4iyixuvfuchjl2o3` FOREIGN KEY (`workspace_id`) REFERENCES `workspace` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `authority` varchar(255) DEFAULT NULL,
                        `date_created` datetime(6) DEFAULT NULL,
                        `date_modified` datetime(6) DEFAULT NULL,
                        `enabled` bit(1) DEFAULT NULL,
                        `external_id` binary(16) NOT NULL,
                        `first_name` varchar(255) NOT NULL,
                        `last_name` varchar(255) NOT NULL,
                        `locale` varchar(255) DEFAULT NULL,
                        `password` varchar(255) DEFAULT NULL,
                        `username` varchar(255) NOT NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `UK_4eu2tvn9rj53a93fufx7ayr20` (`external_id`),
                        UNIQUE KEY `UK_sb8bbouer5wak8vyiiy4pf2bx` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `user_group`;
CREATE TABLE `user_group` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `description` varchar(255) DEFAULT NULL,
                              `external_id` binary(16) NOT NULL,
                              `name` varchar(255) NOT NULL,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `UK_oid76j4848jmwaqf1e8qhi0y2` (`external_id`),
                              UNIQUE KEY `UK_kas9w8ead0ska5n3csefp2bpp` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `user_groups`;
CREATE TABLE `user_groups` (
                               `user_id` bigint NOT NULL,
                               `groups_id` bigint NOT NULL,
                               PRIMARY KEY (`user_id`,`groups_id`),
                               KEY `FK2nhhy7f9gn8ay6bl34e9kex95` (`groups_id`),
                               CONSTRAINT `FK2nhhy7f9gn8ay6bl34e9kex95` FOREIGN KEY (`groups_id`) REFERENCES `user_group` (`id`),
                               CONSTRAINT `FKxgk67l5yp8458l39rog6nppe` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `workspace`;
CREATE TABLE `workspace` (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `date_created` datetime(6) DEFAULT NULL,
                             `date_modified` datetime(6) DEFAULT NULL,
                             `description` varchar(255) DEFAULT NULL,
                             `external_id` binary(16) NOT NULL,
                             `group_permissions` varchar(255) DEFAULT NULL,
                             `name` varchar(255) NOT NULL,
                             `owner_id` bigint DEFAULT NULL,
                             `user_group_id` bigint DEFAULT NULL,
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `UK_5r69hlylku2sqe2v6qn5l1y71` (`external_id`),
                             UNIQUE KEY `UK_br8l0q43h1ygdohbp4htocj3h` (`name`),
                             KEY `FKk7dgp9sb1paeoxv6iudh1snt5` (`owner_id`),
                             KEY `FK4gwkebjndygjcc2q1ekn1hjwk` (`user_group_id`),
                             CONSTRAINT `FK4gwkebjndygjcc2q1ekn1hjwk` FOREIGN KEY (`user_group_id`) REFERENCES `user_group` (`id`),
                             CONSTRAINT `FKk7dgp9sb1paeoxv6iudh1snt5` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 2020-06-13 23:36:21