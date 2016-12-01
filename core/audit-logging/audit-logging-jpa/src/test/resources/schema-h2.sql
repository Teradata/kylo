drop table AUDIT_LOG if exists
create table AUDIT_LOG (id binary(16) not null, CREATE_TIME timestamp, NAME varchar(255) not null, ENTITY_ID binary(16), LOG_TYPE varchar(45) not null, USER varchar(100), primary key (id))
