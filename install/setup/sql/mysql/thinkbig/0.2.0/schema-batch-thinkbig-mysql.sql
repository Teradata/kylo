CREATE TABLE IF NOT EXISTS BATCH_EXECUTION_CONTEXT_VALUES (
                                      JOB_EXECUTION_ID BIGINT NOT NULL,
                                      STEP_EXECUTION_ID BIGINT
                                    , EXECUTION_CONTEXT_TYPE varchar(6) DEFAULT NULL
                                    , TYPE_CD VARCHAR(10) NOT NULL
                                    , KEY_NAME VARCHAR(100) NOT NULL
                                    , STRING_VAL LONGTEXT NULL
                                    , DATE_VAL TIMESTAMP NULL
                                    , LONG_VAL BIGINT NULL
                                    , DOUBLE_VAL DOUBLE PRECISION NULL
                                    , CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL );

CREATE TABLE BATCH_NIFI_STEP  (
  EVENT_ID BIGINT NOT NULL ,
     NIFI_EVENT_ID BIGINT,
     COMPONENT_ID VARCHAR(255),
 JOB_EXECUTION_ID BIGINT,
 STEP_EXECUTION_ID BIGINT
 ) ENGINE=InnoDB;


     CREATE TABLE BATCH_NIFI_JOB  (
     EVENT_ID BIGINT NOT NULL ,
     NIFI_EVENT_ID BIGINT,
     FLOW_FILE_UUID VARCHAR(255),
     FEED_ID BIGINT,
     FEED_NAME VARCHAR(255),
     JOB_INSTANCE_ID BIGINT,
     JOB_EXECUTION_ID BIGINT
     ) ENGINE=InnoDB;

