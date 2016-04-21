
create table if not exists CHANGE_SET (
    id binary(255) not null,
    completenessFactor integer not null,
    intrinsicPeriod varchar(255),
    intrinsicTime datetime,
    dataset_id binary(255),
    primary key (id)
) ENGINE=InnoDB;

create table if not exists CHANGE_SET_FILES (
    id binary(255) not null,
    primary key (id)
) ENGINE=InnoDB;

create table if not exists CHANGE_SET_FILES_PATH (
    change_set_files_id binary(255) not null,
    path varchar(255)
) ENGINE=InnoDB;

create table if not exists CHANGE_SET_HIVE_TABLE (
    recourdCount integer not null,
    id binary(255) not null,
    primary key (id)
) ENGINE=InnoDB;

create table if not exists CHANGE_SET_HIVE_TABLE_PART_VALUE (
    change_set_hive_table_id binary(255) not null,
    name varchar(255),
    value varchar(255)
) ENGINE=InnoDB;

create table if not exists DATA_OPERATION (
    id binary(255) not null,
    startTime datetime,
    state integer,
    status varchar(255),
    stopTime datetime,
    dataset_id binary(255),
    producer_id binary(255),
    primary key (id)
) ENGINE=InnoDB;

create table if not exists DATASET (
    id binary(255) not null,
    time datetime,
    type integer,
    datasource_id binary(255),
    primary key (id)
) ENGINE=InnoDB;

create table if not exists DATASOURCE (
    TYPE varchar(31) not null,
    id binary(255) not null,
    creationTime datetime,
    description varchar(255),
    name varchar(255),
    primary key (id)
) ENGINE=InnoDB;

create table if not exists FEED (
    id binary(255) not null,
    Description varchar(255),
    Name varchar(255),
    sla_id binary(255),
    primary key (id)
) ENGINE=InnoDB;

create table if not exists FEED_DESTINATION (
    id binary(255) not null,
    datasource_id binary(255),
    feed_id binary(255),
    primary key (id)
) ENGINE=InnoDB;

create table if not exists FEED_SOURCE (
    id binary(255) not null,
    datasource_id binary(255),
    feed_id binary(255),
    agreement_id binary(255),
    primary key (id)
) ENGINE=InnoDB;

create table if not exists SLA (
    id binary(255) not null,
    creatingTime datetime,
    description varchar(255),
    name varchar(255),
    primary key (id)
) ENGINE=InnoDB;

create table if not exists SLA_METRIC (
    id binary(255) not null,
    metric varchar(255),
    obligation_id binary(255),
    primary key (id)
) ENGINE=InnoDB;

create table if not exists SLA_OBLIGATION (
    id binary(255) not null,
    description varchar(255),
    group_id binary(255),
    primary key (id)
) ENGINE=InnoDB;

create table if not exists SLA_OBLIGATION_GROUP (
    id binary(255) not null,
    cond integer,
    agreement_id binary(255),
    primary key (id)
) ENGINE=InnoDB;

