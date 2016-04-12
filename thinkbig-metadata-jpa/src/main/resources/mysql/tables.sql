
create table if not exists CHANGE_SET (
    id binary(255) not null, 
    completenessFactor integer not null, 
    intrinsicPeriod tinyblob, 
    intrinsicTime datetime, 
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
    uuid binary(255) not null, 
    startTime datetime, 
    state integer, 
    status varchar(255), 
    stopTime datetime, 
    dataset_id binary(255), 
    producer_uuid binary(255), 
    primary key (uuid)
) ENGINE=InnoDB;

create table if not exists DATASET (
    id binary(255) not null, 
    time datetime, 
    type integer, 
    datasource_uuid binary(255), 
    primary key (id)
) ENGINE=InnoDB;

create table if not exists DATASET_CHANGE_SET (
    JpaDataset_id binary(255) not null, 
    changes_id binary(255) not null, 
    primary key (JpaDataset_id, changes_id)
) ENGINE=InnoDB;

create table if not exists DATASOURCE (
    TYPE varchar(31) not null, 
    uuid binary(255) not null, 
    creationTime datetime, 
    description varchar(255), 
    name varchar(255), 
    primary key (uuid)
) ENGINE=InnoDB;

create table if not exists FEED (
    uuid binary(255) not null, 
    Description varchar(255), 
    Name varchar(255), 
    sla_uuid binary(255), 
    primary key (uuid)
) ENGINE=InnoDB;

create table if not exists FEED_DESTINATION (
    uuid binary(255) not null, 
    dataset_uuid binary(255), 
    feed_uuid binary(255), 
    primary key (uuid)
) ENGINE=InnoDB;

create table if not exists FEED_SOURCE (
    uuid binary(255) not null, 
    dataset_uuid binary(255), 
    feed_uuid binary(255), 
    agreemenet_uuid binary(255), 
    primary key (uuid)
) ENGINE=InnoDB;

create table if not exists SLA (
    uuid binary(255) not null, 
    creatingTime datetime, 
    description varchar(255), 
    name varchar(255), 
    primary key (uuid)
) ENGINE=InnoDB;

create table if not exists SLA_METRIC (
    id binary(255) not null, 
    metric varchar(255), 
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
    agreement_uuid binary(255), 
    primary key (id)
) ENGINE=InnoDB;

create table if not exists SLA_OBLIGATION_SLA_METRIC (
    JpaObligation_id binary(255) not null, 
    metricWrappers_id binary(255) not null, 
    primary key (JpaObligation_id, metricWrappers_id)
) ENGINE=InnoDB;

