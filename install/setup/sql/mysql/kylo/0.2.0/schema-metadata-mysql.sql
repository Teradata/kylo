create table CHANGE_SET (id binary(16) not null, created_time datetime, modified_time datetime, completeness_factor integer, intrinsic_period varchar(255), intrinsic_time datetime, dataset_id binary(16), primary key (id)) ENGINE=InnoDB;
create table CHANGE_SET_FILES (id binary(16) not null, primary key (id)) ENGINE=InnoDB;
create table CHANGE_SET_FILES_PATH (change_set_files_id binary(16) not null, path varchar(255)) ENGINE=InnoDB;
create table CHANGE_SET_HIVE_TABLE (record_count integer, id binary(16) not null, primary key (id)) ENGINE=InnoDB;
create table CHANGE_SET_HIVE_TABLE_PART_VALUE (change_set_hive_table_id binary(16) not null, name varchar(255), value varchar(255)) ENGINE=InnoDB;
create table DATA_OPERATION (id binary(16) not null, created_time datetime, modified_time datetime, start_time datetime, state varchar(15), status varchar(2048), stop_time datetime, dataset_id binary(16), producer_id binary(16), primary key (id)) ENGINE=InnoDB;
create table DATASET (id binary(16) not null, created_time datetime, modified_time datetime, type varchar(10), datasource_id binary(16), primary key (id)) ENGINE=InnoDB;
create table DATASOURCE (type varchar(31) not null, id binary(16) not null, created_time datetime, modified_time datetime, description varchar(255), name varchar(100), database_name varchar(255), table_name varchar(255), path varchar(255), primary key (id)) ENGINE=InnoDB;
create table FEED_DESTINATION (id binary(16) not null, created_time datetime, modified_time datetime, datasource_id binary(16), feed_id binary(16), primary key (id)) ENGINE=InnoDB;
create table FEED_PROPERTIES (JpaFeed_id binary(16) not null, prop_value varchar(255), prop_key varchar(100) not null, primary key (JpaFeed_id, prop_key)) ENGINE=InnoDB;
create table FEED_SOURCE (id binary(16) not null, created_time datetime, modified_time datetime, datasource_id binary(16), feed_id binary(16), agreement_id binary(16), primary key (id)) ENGINE=InnoDB;
create table SLA (id binary(16) not null, created_time datetime, modified_time datetime, description varchar(255), name varchar(100), primary key (id)) ENGINE=InnoDB;
create table SLA_METRIC (id binary(255) not null, created_time datetime, modified_time datetime, metric varchar(255), obligation_id binary(16), primary key (id)) ENGINE=InnoDB;
create table SLA_OBLIGATION (id binary(16) not null, created_time datetime, modified_time datetime, description varchar(255), group_id binary(16), primary key (id)) ENGINE=InnoDB;
create table SLA_OBLIGATION_GROUP (id binary(16) not null, created_time datetime, modified_time datetime, cond varchar(10), agreement_id binary(16), primary key (id)) ENGINE=InnoDB;

CREATE TABLE `CATEGORY` (
  `id` binary(16) NOT NULL,
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `display_name` varchar(100) DEFAULT NULL,
  `state` varchar(45) DEFAULT 'ENABLED',
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `FEED` (
  `id` binary(16) NOT NULL,
  `created_time` datetime DEFAULT NULL,
  `modified_time` datetime DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `display_name` varchar(100) DEFAULT NULL,
  `initialized` char(1) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `state` varchar(10) DEFAULT 'ENABLED',
  `sla_id` binary(16) DEFAULT NULL,
  `version` int(11) DEFAULT '1',
   `category_id` binary(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_m3uusi4w4t57ey2153r1pw7t2` (`name`),
  UNIQUE KEY `UK_j4px0sd8c2k3ycpw6uvqpl1c6` (`display_name`),
  KEY `FKi6tlfq6nytlrb8429acoovlay` (`sla_id`),
  CONSTRAINT `FKi6tlfq6nytlrb8429acoovlay` FOREIGN KEY (`sla_id`) REFERENCES `SLA` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `FM_CATEGORY` (
  `id` binary(16) NOT NULL,
  `icon` varchar(45) DEFAULT NULL,
  `icon_color` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `FM_TEMPLATE` (
  `id` binary(16) NOT NULL,
  `created_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `state` varchar(10) DEFAULT 'ENABLED',
  `is_define_table` varchar(1) DEFAULT NULL,
  `is_data_transform` varchar(1) DEFAULT NULL,
  `allow_preconditions` varchar(1) DEFAULT NULL,
  `json` mediumtext NOT NULL,
  `nifi_template_id` varchar(45) DEFAULT NULL,
  `icon` varchar(45) DEFAULT NULL,
  `icon_color` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `FM_FEED` (
  `id` binary(16) NOT NULL,
  `json` mediumtext,
  `template_id` binary(16) NOT NULL,
  `nifi_process_group_id` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `FM_TEMPLATE_ID_FK_idx` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;




