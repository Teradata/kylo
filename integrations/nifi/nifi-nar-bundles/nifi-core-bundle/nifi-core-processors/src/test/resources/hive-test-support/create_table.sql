create database IF NOT EXISTS ${hiveconf:my.schema};

USE ${hiveconf:my.schema};

CREATE EXTERNAL TABLE foo (i int, s string)
  ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
  STORED AS TEXTFILE
  LOCATION '${hiveconf:MY.HDFS.DIR}/foo/';

create database emp_sr;

CREATE EXTERNAL TABLE emp_sr.`employee`(
  `id` int,
  `name` string,
  `company` string,
  `zip` string,
  `phone` string,
  `email` string,
  `hired` date)
PARTITIONED BY (
  `country` string,
  `year` int)
ROW FORMAT SERDE
  'org.apache.hadoop.hive.ql.io.orc.OrcSerde'
STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.orc.OrcInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat'
  LOCATION '${hiveconf:MY.HDFS.DIR}/emp_sr/employee/'
TBLPROPERTIES (
  'orc.compress'='SNAPPY');


CREATE EXTERNAL TABLE emp_sr.`employee_np`(
`id` int,
`name` string,
`company` string,
`zip` string,
`phone` string,
`email` string,
`hired` date,
`country` string
)
ROW FORMAT SERDE
'org.apache.hadoop.hive.ql.io.orc.OrcSerde'
STORED AS INPUTFORMAT
'org.apache.hadoop.hive.ql.io.orc.OrcInputFormat'
OUTPUTFORMAT
'org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat'
LOCATION '${hiveconf:MY.HDFS.DIR}/emp_sr/employee/'
TBLPROPERTIES (
'orc.compress'='SNAPPY');


CREATE EXTERNAL TABLE emp_sr.`employee_valid`(
  `id` int,
  `name` string,
  `company` string,
  `zip` string,
  `phone` string,
  `email` string,
  `hired` date,
  `country` string
  )
PARTITIONED BY (
  `processing_dttm` string)
ROW FORMAT DELIMITED
  FIELDS TERMINATED BY ','
  LINES TERMINATED BY '\n'
STORED AS INPUTFORMAT
  'org.apache.hadoop.mapred.TextInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
  LOCATION '${hiveconf:MY.HDFS.DIR}/emp_sr/employee_valid/';

set hive.exec.dynamic.partition=true;
set hive.exec.dynamic.partition.mode=nonstrict;

insert into emp_sr.employee_valid partition(processing_dttm='20160119074340') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`)
  values (1,'Sally','ABC','94550','555-1212','sally@acme.org','2015-01-01','USA');
insert into emp_sr.employee_valid partition(processing_dttm='20160119074340') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`)
  values (2,'Joe','ABC','94550','555-1212','sally@acme.org','2016-01-01','USA');
insert into emp_sr.employee_valid partition(processing_dttm='20160119074340') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`)
  values (3,'Bob','ABC','94550','555-1212','sally@acme.org','2015-01-01','Canada');
insert into emp_sr.employee_valid partition(processing_dttm='20160119074340') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`)
  values (4,'Jen','ABC','94550','555-1212','sally@acme.org','2016-01-01','Canada');

insert into emp_sr.employee_valid partition(processing_dttm='20150119074340') (  `id`,  `name`,`company`,`zip`,`phone`,`email`,  `hired`,`country`)
  values (5,'Lisa','ABC','94550','555-1212','sally@acme.org','2015-01-01','USA');

MSCK REPAIR TABLE emp_sr.employee_valid;
