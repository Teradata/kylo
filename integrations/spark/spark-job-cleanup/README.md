Spark Cleanup Job
==========

### Overview
A Spark job capable of performing cleanup of Hive tables, HDFS folders and local folders, as per a retention schedule.

### How it works
TODO: Document me

### Execution
TODO: Document me

_For skeleton layout, the below commands will perform a rowcount on the Hive schema.table_

***Build:***
mvn clean install package

**Spark 1:**
spark-submit --class com.thinkbiganalytics.spark.cleanup.Cleanup --master yarn-client /path/to/kylo-spark-job-cleanup-spark-v1-0.8.0-SNAPSHOT-jar-with-dependencies.jar \<hive-schema> \<hive-table>

**Spark 2:**
spark-submit --class com.thinkbiganalytics.spark.cleanup.Cleanup --master yarn-client /path/to/kylo-spark-job-cleanup-spark-v2-0.8.0-SNAPSHOT-jar-with-dependencies.jar \<hive-schema> \<hive-table>


### Example Attributes file (JSON)

```javascript

TODO: Document me

```