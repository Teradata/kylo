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

spark-submit --master <master> --class com.thinkbiganalytics.spark.dataprofiler.core.Profiler target/dataprofiler-0.1.jar <object_type> <object_desc> <n_for_topn> <output_table>

Valid values:
<master>			: {spark master}
<object_type>		: {"table", "query"}
<object_desc>		: {database.table, query}
<n_for_topn>		: {integer n for top n items}
<output_table>		: {name of table to write result to}


- An example command:
spark-submit --master local[*] --class com.thinkbiganalytics.spark.dataprofiler.core.Profiler target/dataprofiler-0.1.jar table profiler.people 3 peoplestatistics

- The data will be read from the provided database.table/query result, and profile statistics will be written to output table in Hive default database.

- Output result table format (all columns are strings):

-------------|------------|--------------
| ColumnName | MetricType | MetricValue |
-------------|------------|--------------

- Refer to docs/sample_output.txt for example of output format


==============
Documentation
==============
- Inline code comments
- API Documentation available at target/apidocs/index.html
- docs/profiler-metric-mapping.xls
- docs/sample_output.txt

