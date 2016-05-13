create table IF NOT EXISTS CHANGE_SET (id UUID not null, created_time timestamp, modified_time timestamp, completeness_factor integer, intrinsic_period varchar(255), intrinsic_time timestamp, dataset_id UUID, primary key (id));
create table IF NOT EXISTS CHANGE_SET_FILES (id UUID not null, primary key (id));
create table IF NOT EXISTS CHANGE_SET_FILES_PATH (change_set_files_id UUID not null, path varchar(255));
create table IF NOT EXISTS CHANGE_SET_HIVE_TABLE (record_count integer, id UUID not null, primary key (id));
create table IF NOT EXISTS CHANGE_SET_HIVE_TABLE_PART_VALUE (change_set_hive_table_id UUID not null, name varchar(255), value varchar(255));
create table IF NOT EXISTS DATA_OPERATION (id UUID not null, created_time timestamp, modified_time timestamp, start_time timestamp, state varchar(15), status varchar(2048), stop_time timestamp, dataset_id UUID, producer_id UUID, primary key (id));
create table IF NOT EXISTS DATASET (id UUID not null, created_time timestamp, modified_time timestamp, type varchar(10), datasource_id UUID, primary key (id));
create table IF NOT EXISTS DATASOURCE (type varchar(31) not null, id UUID not null, created_time timestamp, modified_time timestamp, description varchar(255), name varchar(100), database_name varchar(255), table_name varchar(255), path varchar(255), primary key (id));
create table IF NOT EXISTS FEED_DESTINATION (id UUID not null, created_time timestamp, modified_time timestamp, datasource_id UUID, feed_id UUID, primary key (id));
create table IF NOT EXISTS FEED_PROPERTIES (JpaFeed_id UUID not null, prop_value varchar(255), prop_key varchar(100) not null, primary key (JpaFeed_id, prop_key));
create table IF NOT EXISTS FEED_SOURCE (id UUID not null, created_time timestamp, modified_time timestamp, datasource_id UUID, feed_id UUID, agreement_id UUID, primary key (id));
create table IF NOT EXISTS SLA (id UUID not null, created_time timestamp, modified_time timestamp, description varchar(255), name varchar(100), primary key (id));
create table IF NOT EXISTS SLA_METRIC (id UUID not null, created_time timestamp, modified_time timestamp, metric varchar(255), obligation_id UUID, primary key (id));
create table IF NOT EXISTS SLA_OBLIGATION (id UUID not null, created_time timestamp, modified_time timestamp, description varchar(255), group_id UUID, primary key (id));
create table IF NOT EXISTS SLA_OBLIGATION_GROUP (id UUID not null, created_time timestamp, modified_time timestamp, cond varchar(10), agreement_id UUID, primary key (id));


CREATE TABLE IF NOT EXISTS CATEGORY (
  id UUID NOT NULL,
  created_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_time timestamp,
  description varchar(255) DEFAULT NULL,
  name varchar(100) NOT NULL,
  display_name varchar(100) DEFAULT NULL,
  state varchar(45) DEFAULT 'ENABLED',
  version int DEFAULT 1,
  PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS FEED (
  id UUID NOT NULL,
  created_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_time timestamp DEFAULT NULL,
  description varchar(255) DEFAULT NULL,
  display_name varchar(100) DEFAULT NULL,
  initialized char(1) DEFAULT NULL,
  name varchar(100) NOT NULL,
  state varchar(10) DEFAULT 'NEW',
  sla_id UUID DEFAULT NULL,
  version int DEFAULT '1',
   category_id UUID DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FKi6tlfq6nytlrb8429acoovlay FOREIGN KEY (sla_id) REFERENCES SLA (id)
);

CREATE TABLE IF NOT EXISTS FM_CATEGORY (
  id UUID NOT NULL,
  icon varchar(45) DEFAULT NULL,
  icon_color varchar(45) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS FM_TEMPLATE (
  id UUID NOT NULL,
  created_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_time timestamp,
  description varchar(255) DEFAULT NULL,
  name varchar(255) NOT NULL,
  state varchar(10) DEFAULT 'ENABLED',
  is_define_table varchar(1) DEFAULT NULL,
  is_data_transform varchar(1) DEFAULT NULL,
  allow_preconditions varchar(1) DEFAULT NULL,
  json TEXT NOT NULL,
  nifi_template_id varchar(45) DEFAULT NULL,
  icon varchar(45) DEFAULT NULL,
  icon_color varchar(45) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS FM_FEED (
  id UUID NOT NULL,
  json TEXT,
  template_id UUID NOT NULL,
  nifi_process_group_id varchar(45) DEFAULT NULL,
  PRIMARY KEY (id)
);




