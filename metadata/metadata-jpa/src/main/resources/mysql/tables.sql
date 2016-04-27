
create table CHANGE_SET (
    id binary(16) not null, 
    completeness_factor integer, 
    intrinsic_period varchar(255), 
    intrinsic_time datetime, 
    dataset_id binary(16), 
    primary key (id)
) ENGINE=InnoDB;

create table CHANGE_SET_FILES (
    id binary(16) not null, 
    primary key (id)
) ENGINE=InnoDB;

create table CHANGE_SET_FILES_PATH (
    change_set_files_id binary(16) not null, 
    path varchar(255)
) ENGINE=InnoDB;

create table CHANGE_SET_HIVE_TABLE (
    record_count integer, 
    id binary(16) not null, 
    primary key (id)
) ENGINE=InnoDB;

create table CHANGE_SET_HIVE_TABLE_PART_VALUE (
    change_set_hive_table_id binary(16) not null, 
    name varchar(100), 
    value varchar(255)
) ENGINE=InnoDB;

create table DATA_OPERATION (
    id binary(16) not null, 
    start_time datetime, 
    state integer, 
    status varchar(255), 
    stop_time datetime, 
    dataset_id binary(16), 
    producer_id binary(16), 
    primary key (id)
) ENGINE=InnoDB;

create table DATASET (
    id binary(16) not null, 
    created_time datetime, 
    type integer, 
    datasource_id binary(16), 
    primary key (id)
) ENGINE=InnoDB;

create table DATASOURCE (
    TYPE varchar(31) not null, 
    id binary(16) not null, 
    created_time datetime, 
    description varchar(255), 
    name varchar(100), 
    primary key (id)
) ENGINE=InnoDB;

create table FEED (
    id binary(16) not null, 
    description varchar(255), 
    name varchar(100), 
    sla_id binary(16), 
    primary key (id)
) ENGINE=InnoDB;

create table FEED_DESTINATION (
    id binary(16) not null, 
    datasource_id binary(16), 
    feed_id binary(16), 
    primary key (id)
) ENGINE=InnoDB;

create table FEED_SOURCE (
    id binary(16) not null, 
    datasource_id binary(16), 
    feed_id binary(16), 
    agreement_id binary(16), 
    primary key (id)
) ENGINE=InnoDB;

create table SLA (
    id binary(16) not null, 
    created_time datetime, 
    description varchar(255), 
    name varchar(100), 
    primary key (id)
) ENGINE=InnoDB;

create table SLA_METRIC (
    id binary(255) not null, 
    metric varchar(255), 
    obligation_id binary(16), 
    primary key (id)
) ENGINE=InnoDB;

create table SLA_OBLIGATION (
    id binary(16) not null, 
    description varchar(255), 
    group_id binary(16), 
    primary key (id)
) ENGINE=InnoDB;

create table SLA_OBLIGATION_GROUP (
    id binary(16) not null, 
    cond integer, 
    agreement_id binary(16), 
    primary key (id)
) ENGINE=InnoDB;


