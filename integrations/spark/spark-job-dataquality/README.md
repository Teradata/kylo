Spark Data Quality Job
==========

### Overview
A Spark job capable of performing data quality checks as per provided rules.

### How it works
TODO: Document me

### Execution
TODO: Document me

_For skeleton layout, the below commands will perform a rowcount on the Hive schema.table_

***Build:***
mvn clean install package

**Spark 1:**
spark-submit --class com.thinkbiganalytics.spark.dataquality.checker.DataQualityChecker --master yarn-client /path/to/kylo-spark-job-dataquality-spark-v1-0.8.0-SNAPSHOT-jar-with-dependencies.jar \<hive-schema> \<hive-table>

**Spark 2:**
spark-submit --class com.thinkbiganalytics.spark.dataquality.checker.DataQualityChecker --master yarn-client /path/to/kylo-spark-job-dataquality-spark-v2-0.8.0-SNAPSHOT-jar-with-dependencies.jar \<hive-schema> \<hive-table>


### Example Attributes file (JSON)

```javascript

TODO: Document me

```