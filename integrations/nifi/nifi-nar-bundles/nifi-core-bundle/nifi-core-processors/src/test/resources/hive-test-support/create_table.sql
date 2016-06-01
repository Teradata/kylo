USE ${hiveconf:my.schema};

CREATE EXTERNAL TABLE foo (i int, s string)
  ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
  STORED AS TEXTFILE
  LOCATION '${hiveconf:MY.HDFS.DIR}/foo/';