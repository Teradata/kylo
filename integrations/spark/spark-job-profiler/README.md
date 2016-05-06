Spark Data Profiler job
==========

### Overview

A Spark job capable of performing generating profile statistics against a source table, partition, or for a provided query.  

### How it works

1. Data is typically read from a source table such as <entity>-valid and a given partition
2. Profile statistics are generated
3. Profiler statistics is written to <entity>-profile

### Execution

The project is a spark-submit job:

./bin/spark-submit \
  --class com.thinkbiganalytics.spark.dataprofiler.core.Profiler \
  --master yarn-client \
  /path/to/jar/thinkbig-spark-job-profiler-<version>-jar-with-dependencies.jar \
  table <database.source_table> <integer> <database.output_table> <partition>

Command-line arguments:
* object type - valid values are: table or query
* object description - valid values are: <database.table> or <query>
* top_n - <integer> as amount of top n values to include in the sampling
* output table - <table>, <database.table>
* partition_key - partition name  (optional parameter)
