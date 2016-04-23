Think Big Analytics - Internal Asset
------------------------------------

=============
Asset Details
=============
Name: Data Profiler
Version: 0.1
Developer: Jagrut Sharma
Date: 01-27-2016


============
Introduction
============
- This program can be used to profile statistics of a Hive table's data, and write results back to Hive.
- Refer to docs/profiler-metric-mapping.xls for list of metrics which are profiled for each Hive data type.
- Requires these or higher versions: Java 1.7, Spark version 1.4.1


========
Building
========
console> mvn clean compile package install

Ensure that all unit tests pass.



=======
Running
=======

spark-submit --master <master> --class com.thinkbiganalytics.spark.dataprofiler.core.Profiler target/thinkbig-spark-0.0.1-SNAPSHOT.jar <object_type> <object_desc> <n_for_topn> <output_db_table> <optional: partition_key>

Valid argument values
---------------------
<master>			: {spark master}

<object_type>		: {"table", "query"}

<object_desc>		: {database.table, query} 
					  - Ensure that the database referred in query or table exists in Hive
					  
<n_for_topn>		: {integer n for top n items}

<output_table>		: {database.table, table}
					  - The location to write results to. 
					  - Only alphanumeric and underscore characters allowed for table name.
					  - Ensure database exists in Hive. If no database is specified, 'default' is used.
					  - This is a partitioned table, and partition column should be named 'processing_dttm'.
					  - Table will be created automatically if not available.

<partition_key>		: {string}
					  - Optional parameter. 
					  - The partition to read from and write results to. 
					  - Only alphanumeric and underscore characters allowed.
					  - Input and output tables both have the same value of partition key.
					  - Input and and output tables should have partition column named 'processing_dttm'.
					  - If not specified:
					  		- entire input table will be considered for processing.
					  		- results will be written to a partition key 'ALL' in output table.

Sample usages
-------------

#1:
spark-submit --master local[*] --class com.thinkbiganalytics.spark.dataprofiler.core.Profiler target/thinkbig-spark-0.0.1-SNAPSHOT.jar table profiler.people 3 peoplestatistics


#2:
spark-submit --master local[*] --class com.thinkbiganalytics.spark.dataprofiler.core.Profiler target/thinkbig-spark-0.0.1-SNAPSHOT.jar table profiler.people 3 statsdb.peoplestatistics


#3:
(Note: both profiler.people and statsdb.peoplestatistics tables should have the partition: processing_dttm='part_001')
spark-submit --master local[*] --class com.thinkbiganalytics.spark.dataprofiler.core.Profiler target/thinkbig-spark-0.0.1-SNAPSHOT.jar table profiler.people 3 statsdb.peoplestatistics part_001


#4:
spark-submit --master local[*] --class com.thinkbiganalytics.spark.dataprofiler.core.Profiler target/thinkbig-spark-0.0.1-SNAPSHOT.jar query "select * from profiler.people where id >= 4" 3 statsdb.peoplestatistics part_002



Output result
-------------
- Format
+-------------+------------------------+------------------------------+
| columnname  |       metricname       |         metricvalue          |
+-------------+------------------------+------------------------------+

- All columns are strings
- Refer to docs/sample_output.txt for example of output format


==============
Documentation
==============
- Inline code comments
- API Documentation available at target/apidocs/index.html
- docs/profiler-metric-mapping.xls
- docs/sample_output.txt

