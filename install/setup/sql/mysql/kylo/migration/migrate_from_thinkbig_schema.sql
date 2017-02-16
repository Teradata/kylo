use kylo;

SET FOREIGN_KEY_CHECKS=0;

insert into kylo.AUDIT_LOG select * from thinkbig.AUDIT_LOG;

insert into kylo.BATCH_EXECUTION_CONTEXT_VALUES select * from thinkbig.BATCH_EXECUTION_CONTEXT_VALUES;

insert into kylo.BATCH_JOB_EXECUTION select * from thinkbig.BATCH_JOB_EXECUTION;

insert into kylo.BATCH_JOB_EXECUTION_CTX_VALS select * from thinkbig.BATCH_JOB_EXECUTION_CTX_VALS;

insert into kylo.BATCH_JOB_EXECUTION_PARAMS select * from thinkbig.BATCH_JOB_EXECUTION_PARAMS;

-- insert into kylo.BATCH_JOB_EXECUTION_SEQ select * from thinkbig.BATCH_JOB_EXECUTION_SEQ;

insert into kylo.BATCH_JOB_INSTANCE select * from thinkbig.BATCH_JOB_INSTANCE;

-- insert into kylo.BATCH_JOB_SEQ select * from thinkbig.BATCH_JOB_SEQ;

insert into kylo.BATCH_NIFI_JOB select * from thinkbig.BATCH_NIFI_JOB;

insert into kylo.BATCH_NIFI_STEP select * from thinkbig.BATCH_NIFI_STEP;

insert into kylo.BATCH_STEP_EXECUTION select * from thinkbig.BATCH_STEP_EXECUTION;

insert into kylo.BATCH_STEP_EXECUTION_CTX_VALS select * from thinkbig.BATCH_STEP_EXECUTION_CTX_VALS;

-- insert into kylo.BATCH_STEP_EXECUTION_SEQ select * from thinkbig.BATCH_STEP_EXECUTION_SEQ;

insert into kylo.FEED select * from thinkbig.FEED;

insert into kylo.FEED_CHECK_DATA_FEEDS select * from thinkbig.FEED_CHECK_DATA_FEEDS;

-- insert into kylo.GENERATED_KEYS select * from thinkbig.GENERATED_KEYS;

update kylo.GENERATED_KEYS
set VALUE_COLUMN = (SELECT VALUE_COLUMN FROM thinkbig.GENERATED_KEYS WHERE PK_COLUMN='JOB_EXECUTION_ID')
WHERE PK_COLUMN='JOB_EXECUTION_ID';

update kylo.GENERATED_KEYS
set VALUE_COLUMN = (SELECT VALUE_COLUMN FROM thinkbig.GENERATED_KEYS WHERE PK_COLUMN='JOB_INSTANCE_ID')
WHERE PK_COLUMN='JOB_INSTANCE_ID';

update kylo.GENERATED_KEYS
set VALUE_COLUMN = (SELECT VALUE_COLUMN FROM thinkbig.GENERATED_KEYS WHERE PK_COLUMN='STEP_EXECUTION_ID')
WHERE PK_COLUMN='STEP_EXECUTION_ID';

insert into kylo.KYLO_ALERT select * from thinkbig.KYLO_ALERT;

insert into kylo.KYLO_ALERT_CHANGE select * from thinkbig.KYLO_ALERT_CHANGE;

insert into kylo.KYLO_VERSION select * from thinkbig.KYLO_VERSION;

CREATE TABLE IF NOT EXISTS  kylo.MODESHAPE_REPOSITORY (
  ID varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  LAST_CHANGED timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONTENT longblob NOT NULL,
  PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into kylo.MODESHAPE_REPOSITORY select * from thinkbig.MODESHAPE_REPOSITORY;

insert into kylo.NIFI_EVENT select * from thinkbig.NIFI_EVENT;

insert into kylo.NIFI_FEED_PROCESSOR_STATS select * from thinkbig.NIFI_FEED_PROCESSOR_STATS;

insert into kylo.NIFI_RELATED_ROOT_FLOW_FILES select * from thinkbig.NIFI_RELATED_ROOT_FLOW_FILES;

insert into kylo.SLA_ASSESSMENT select * from thinkbig.SLA_ASSESSMENT;

insert into kylo.SLA_METRIC_ASSESSMENT select * from thinkbig.SLA_METRIC_ASSESSMENT;

insert into kylo.SLA_OBLIGATION_ASSESSMENT select * from thinkbig.SLA_OBLIGATION_ASSESSMENT;

SET FOREIGN_KEY_CHECKS=1;

commit;