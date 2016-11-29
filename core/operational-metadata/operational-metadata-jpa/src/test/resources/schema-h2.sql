drop table BATCH_FEED_SUMMARY_COUNTS_VW if exists
drop table BATCH_JOB_EXECUTION if exists
drop table BATCH_JOB_EXECUTION_CONTEXT if exists
drop table BATCH_JOB_EXECUTION_CTX_VALS if exists
drop table BATCH_JOB_EXECUTION_PARAMS if exists
drop table BATCH_JOB_INSTANCE if exists
drop table BATCH_NIFI_JOB if exists
drop table BATCH_NIFI_STEP if exists
drop table BATCH_STEP_EXECUTION if exists
drop table BATCH_STEP_EXECUTION_CONTEXT if exists
drop table BATCH_STEP_EXECUTION_CTX_VALS if exists
drop table FEED if exists
drop table FEED_CHECK_DATA_FEEDS if exists
drop table GENERATED_KEYS if exists
drop table KYLO_VERSION if exists
drop table LATEST_FEED_JOB_EXECUTION_VW if exists
drop table NIFI_EVENT if exists
drop table NIFI_FEED_PROCESSOR_STATS if exists
drop table NIFI_RELATED_ROOT_FLOW_FILES if exists
drop table SLA_ASSESSMENT if exists
drop table SLA_METRIC_ASSESSMENT if exists
drop table SLA_OBLIGATION_ASSESSMENT if exists
create table BATCH_FEED_SUMMARY_COUNTS_VW (feed_id binary(16) not null, ABANDONED_COUNT bigint, ALL_COUNT bigint, COMPLETED_COUNT bigint, FAILED_COUNT bigint, FEED_NAME varchar(255), primary key (feed_id))
create table BATCH_JOB_EXECUTION (JOB_EXECUTION_ID bigint not null, CREATE_TIME timestamp, END_TIME timestamp, EXIT_CODE varchar(255), EXIT_MESSAGE varchar(255), LAST_UPDATED timestamp, START_TIME timestamp, STATUS varchar(10) not null, VERSION bigint, JOB_INSTANCE_ID bigint not null, primary key (JOB_EXECUTION_ID))
create table BATCH_JOB_EXECUTION_CONTEXT (JOB_EXECUTION_ID bigint not null, SERIALIZED_CONTEXT clob, SHORT_CONTEXT varchar(255), primary key (JOB_EXECUTION_ID))
create table BATCH_JOB_EXECUTION_CTX_VALS (id varchar(255) not null, DATE_VAL timestamp, DOUBLE_VAL double, LONG_VAL bigint, STRING_VAL varchar(255), TYPE_CD varchar(10) not null, JOB_EXECUTION_ID bigint, KEY_NAME varchar(255), primary key (id))
create table BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID bigint not null, KEY_NAME varchar(255) not null, DATE_VAL timestamp, DOUBLE_VAL double, LONG_VAL bigint, STRING_VAL varchar(255), TYPE_CD varchar(10) not null, primary key (JOB_EXECUTION_ID, KEY_NAME))
create table BATCH_JOB_INSTANCE (JOB_INSTANCE_ID bigint not null, JOB_KEY varchar(255), JOB_NAME varchar(255), VERSION bigint, FEED_ID binary(16), primary key (JOB_INSTANCE_ID))
create table BATCH_NIFI_JOB (FLOW_FILE_ID varchar(255) not null, EVENT_ID bigint, JOB_EXECUTION_ID bigint not null, primary key (FLOW_FILE_ID))
create table BATCH_NIFI_STEP (EVENT_ID bigint not null, FLOW_FILE_ID varchar(255) not null, COMPONENT_ID varchar(255), JOB_FLOW_FILE_ID varchar(255), JOB_EXECUTION_ID bigint not null, STEP_EXECUTION_ID bigint not null, primary key (EVENT_ID, FLOW_FILE_ID))
create table BATCH_STEP_EXECUTION (STEP_EXECUTION_ID bigint not null, END_TIME timestamp, EXIT_CODE varchar(255), EXIT_MESSAGE varchar(255), LAST_UPDATED timestamp, START_TIME timestamp, STATUS varchar(10) not null, STEP_NAME varchar(255), VERSION bigint, JOB_EXECUTION_ID bigint not null, primary key (STEP_EXECUTION_ID))
create table BATCH_STEP_EXECUTION_CONTEXT (STEP_EXECUTION_ID bigint not null, SERIALIZED_CONTEXT clob, SHORT_CONTEXT varchar(255), primary key (STEP_EXECUTION_ID))
create table BATCH_STEP_EXECUTION_CTX_VALS (id varchar(255) not null, DATE_VAL timestamp, DOUBLE_VAL double, LONG_VAL bigint, STRING_VAL varchar(255), TYPE_CD varchar(10) not null, JOB_EXECUTION_ID bigint, KEY_NAME varchar(255), STEP_EXECUTION_ID bigint, primary key (id))
create table FEED (id binary(16) not null, FEED_TYPE varchar(255), name varchar(100) not null, primary key (id))
create table FEED_CHECK_DATA_FEEDS (FEED_ID binary(16) not null, CHECK_DATA_FEED_ID binary(16) not null, primary key (FEED_ID, CHECK_DATA_FEED_ID))
create table GENERATED_KEYS (PK_COLUMN varchar(255) not null, VALUE_COLUMN bigint, primary key (PK_COLUMN))
create table KYLO_VERSION (id binary not null, created_time timestamp, modified_time timestamp, description varchar(255), MAJOR_VERSION varchar(255), MINOR_VERSION varchar(255), primary key (id))
create table LATEST_FEED_JOB_EXECUTION_VW (JOB_EXECUTION_ID bigint not null, END_TIME timestamp, EXIT_CODE varchar(255), EXIT_MESSAGE varchar(255), id binary(16), FEED_NAME varchar(255), START_TIME timestamp, STATUS varchar(10) not null, FEED_ID binary(16), primary key (JOB_EXECUTION_ID))
create table NIFI_EVENT (EVENT_ID bigint not null, FLOW_FILE_ID varchar(255) not null, created_time timestamp, modified_time timestamp, ATTRIBUTES_JSON varchar(255), CHILD_FLOW_FILE_IDS varchar(255), EVENT_DETAILS varchar(255), EVENT_DURATION_MILLIS bigint, EVENT_TIME timestamp, EVENT_TYPE varchar(255), FM_FEED_NAME varchar(255), FEED_PROCESS_GROUP_ID varchar(255), FILE_SIZE varchar(255), FILE_SIZE_BYTES bigint, HAS_FAILURE_EVENTS char(1), IS_BATCH_JOB char(1), IS_END_OF_JOB char(1), IS_FAILURE char(1), IS_FINAL_JOB_EVENT char(1), IS_START_OF_JOB char(1), JOB_FLOW_FILE_ID varchar(255), PARENT_FLOW_FILE_IDS varchar(255), PROCESSOR_ID varchar(255), PROCESSOR_NAME varchar(255), SOURCE_CONNECTION_ID varchar(255), primary key (EVENT_ID, FLOW_FILE_ID))
create table NIFI_FEED_PROCESSOR_STATS (id varchar(255) not null, BYTES_IN bigint, BYTES_OUT bigint, COLLECTION_ID varchar(255), COLLECTION_TIME timestamp, DURATION_MILLIS bigint, FM_FEED_NAME varchar(255), NIFI_FEED_PROCESS_GROUP_ID varchar(255), FLOW_FILES_FINISHED bigint, FLOW_FILES_STARTED bigint, JOB_DURATION bigint, JOBS_FAILED bigint, JOBS_FINISHED bigint, JOBS_STARTED bigint, MAX_EVENT_TIME timestamp, MIN_EVENT_TIME timestamp, NIFI_PROCESSOR_ID varchar(255), PROCESSOR_NAME varchar(255), PROCESSORS_FAILED bigint, SUCCESSFUL_JOB_DURATION bigint, TOTAL_EVENTS bigint, primary key (id))
create table NIFI_RELATED_ROOT_FLOW_FILES (FLOW_FILE_ID varchar(255) not null, RELATION_ID varchar(255) not null, EVENT_ID bigint, EVENT_FLOW_FILE_ID varchar(255), primary key (FLOW_FILE_ID, RELATION_ID))
create table SLA_ASSESSMENT (id binary(16) not null, created_time timestamp, modified_time timestamp, MESSAGE varchar(255), RESULT varchar(255), SLA_ID varchar(255), primary key (id))
create table SLA_METRIC_ASSESSMENT (id binary(16) not null, created_time timestamp, modified_time timestamp, COMPARABLES varchar(255), MESSAGE varchar(255), METRIC_DESCRIPTION varchar(255), METRIC_TYPE varchar(255), RESULT varchar(255), DATA varchar(255), SLA_OBLIGATION_ASSESSMENT_ID binary(16), primary key (id))
create table SLA_OBLIGATION_ASSESSMENT (id binary(16) not null, created_time timestamp, modified_time timestamp, COMPARABLES varchar(255), MESSAGE varchar(255), OBLIGATION_ID varchar(255), RESULT varchar(255), SLA_ASSESSMENT_ID binary(16), primary key (id))
alter table FEED add constraint UK_m3uusi4w4t57ey2153r1pw7t2 unique (name)
alter table BATCH_FEED_SUMMARY_COUNTS_VW add constraint FKd7uo5rixboy670898vk9mjun8 foreign key (FEED_ID) references FEED
alter table BATCH_JOB_EXECUTION add constraint FKlkdn20f35dqvsxmm0okk22uth foreign key (JOB_INSTANCE_ID) references BATCH_JOB_INSTANCE
alter table BATCH_JOB_EXECUTION_CTX_VALS add constraint FKhwqijwbqbu032m2euh8lljtre foreign key (JOB_EXECUTION_ID) references BATCH_JOB_EXECUTION
alter table BATCH_JOB_EXECUTION_PARAMS add constraint FKimfo9br7bpbtm48288d5dco6v foreign key (JOB_EXECUTION_ID) references BATCH_JOB_EXECUTION
alter table BATCH_JOB_INSTANCE add constraint FKlg59ntt6a357fkdv5n74gs4yf foreign key (FEED_ID) references FEED
alter table BATCH_NIFI_JOB add constraint FKl8iilnip8c8r6c4t9nvl12j1n foreign key (JOB_EXECUTION_ID) references BATCH_JOB_EXECUTION
alter table BATCH_NIFI_STEP add constraint FKfv3ldlgkfdqj8t6e0emkic0gv foreign key (JOB_EXECUTION_ID) references BATCH_JOB_EXECUTION
alter table BATCH_NIFI_STEP add constraint FKd0bj2ulxd8bc3m9cua53t7ggi foreign key (STEP_EXECUTION_ID) references BATCH_STEP_EXECUTION
alter table BATCH_STEP_EXECUTION add constraint FKaui42assgpqx5tn2a64wyy2ng foreign key (JOB_EXECUTION_ID) references BATCH_JOB_EXECUTION
alter table BATCH_STEP_EXECUTION_CTX_VALS add constraint FK6iwq5rjdj20s4kspfal1pxr29 foreign key (STEP_EXECUTION_ID) references BATCH_STEP_EXECUTION
alter table FEED_CHECK_DATA_FEEDS add constraint FKalhqwfdhmfofiy2gugf1050nu foreign key (CHECK_DATA_FEED_ID) references FEED
alter table FEED_CHECK_DATA_FEEDS add constraint FK9d01tbo4cqidkb0uxhlm3jo06 foreign key (FEED_ID) references FEED
alter table LATEST_FEED_JOB_EXECUTION_VW add constraint FKmn5c7wjkmwja8cl1tmw4lm07m foreign key (FEED_ID) references FEED
alter table LATEST_FEED_JOB_EXECUTION_VW add constraint FKbu2xlr61fm24ujdnj3ddnirhd foreign key (JOB_EXECUTION_ID) references BATCH_JOB_EXECUTION
alter table NIFI_RELATED_ROOT_FLOW_FILES add constraint FKib9bm2ce310jonvbahmghrqxa foreign key (EVENT_ID, EVENT_FLOW_FILE_ID) references NIFI_EVENT
alter table NIFI_RELATED_ROOT_FLOW_FILES add constraint FKnqg41lq69s21itdgvrg2c1dgr foreign key (FLOW_FILE_ID) references BATCH_NIFI_JOB
alter table SLA_METRIC_ASSESSMENT add constraint FKgcqi4drad417grw7ocsd1q5xk foreign key (SLA_OBLIGATION_ASSESSMENT_ID) references SLA_OBLIGATION_ASSESSMENT
alter table SLA_OBLIGATION_ASSESSMENT add constraint FKs3qoofshb8oy1lt4r4x2fjxra foreign key (SLA_ASSESSMENT_ID) references SLA_ASSESSMENT