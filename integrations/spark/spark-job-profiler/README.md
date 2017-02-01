Spark Data Profiler
==========

### Overview

A Spark job capable of performing generating profile statistics against a source table, partition, or for a provided query.  

### How it works

1. Data is typically read from a source table such as <entity>-valid and a given partition.
2. Profile statistics are generated.
3. Profiler statistics is written to <entity>-profile table.

### Documentation

* Refer to docs/profiler-metric-mapping.xls for list of metrics which are profiled for each Hive data type.
* Refer to docs/sample_output.txt for sample output.

### Execution

The project is a spark-submit job:

./bin/spark-submit \
  --class com.thinkbiganalytics.spark.dataprofiler.core.Profiler \
  --master yarn-client \
  /path/to/jar/kylo-spark-job-profiler-\<version>-jar-with-dependencies.jar \
  \<object_type> \<object_description> \<top_n_integer> \<output_table> \<partition_key>

Command-line arguments:
* \<object_type> - valid values are: "table", "query"
* \<object_description> - valid values are: "database.table", "query"  
-Database referred in query or table must exist in Hive. 
* \<top_n_integer> - number of top n values to include in the sampling
* \<output table> - valid values are: "table", "database.table"  
-The location to write results to.  
-Only alphanumeric and underscore characters allowed for table name.  
-Ensure database exists in Hive. If no database is specified, 'default' is used.  
-This is a partitioned table, and partition column should be named 'processing_dttm'.  
-Table will be created automatically if not available.
* \<partition_key> - partition name  (optional parameter)  
-The partition to read from and write results to.  
-Only alphanumeric and underscore characters allowed.  
-Input and output tables both have the same value of partition key.  
-Input and and output tables should have partition column named 'processing_dttm'.  
-If not specified: (1) entire input table will be considered for processing. (2) results will be written to a partition key 'ALL' in output table.


####Output Table Format
| columnname | metricname | metricvalue |
|------------|------------|-------------|
