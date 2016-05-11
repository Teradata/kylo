\connect thinkbig;
CREATE TABLE IF NOT EXISTS BATCH_EXECUTION_CONTEXT_VALUES (
                                      JOB_EXECUTION_ID BIGINT NOT NULL,
                                      STEP_EXECUTION_ID BIGINT
                                    , EXECUTION_CONTEXT_TYPE varchar(6) DEFAULT NULL
                                    , TYPE_CD VARCHAR(10) NOT NULL
                                    , KEY_NAME VARCHAR(100) NOT NULL
                                    , STRING_VAL VARCHAR(250) NULL
                                    , DATE_VAL TIMESTAMP NULL
                                    , LONG_VAL BIGINT NULL
                                    , DOUBLE_VAL DOUBLE PRECISION NULL
                                    , CREATE_DATE TIMESTAMP NOT NULL );