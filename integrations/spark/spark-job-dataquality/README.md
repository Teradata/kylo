Spark Data Quality Job
==========

### Overview
A Spark job capable of performing data quality checks as per provided rules.

This Spark job is applicable for flows that pull data using the __GetTableData__ and/or __ImportSqoop__ Nifi processors.
If another source processor is used, the source.row.count attribute needs to be set.

### How it works
This Spark job assumes Hive table naming conventions following standard-ingest processing standards.

1. Attributes from Nifi are passed to the Spark job via command line arguments
2. Data Quality rules are added to the rules list
3. Row counts are done for all hive tables based on the feed and processing_dttm value
4. Each rule is evaluated. If any fail, the job is considered a failured.
5. The results of the rules are stored into a \<feed>\_dataquality Hive table. If this table does not exist, it will be created

The attributes are supplied externally via a JSON file. Steps to create this are supplied below.

### Execution

The project is a spark-submit job:

**Spark 1:**
```
./bin/spark-submit \
--master yarn-client \
--class com.thinkbiganalytics.spark.dataquality.checker.DataQualityChecker \
/path/to/jar/kylo-spark-job-dataquality-spark-v1-<version>-jar-with-dependencies.jar \
</path/to/attributes/file.json>
```

**Spark 2:**
```
./bin/spark-submit \
--master yarn-client \
--class com.thinkbiganalytics.spark.dataquality.checker.DataQualityChecker \
/path/to/jar/kylo-spark-job-dataquality-spark-v2-<version>-SNAPSHOT-jar-with-dependencies.jar \
</path/to/attributes/file.json>
```

### Build ###
mvn clean install package


### Example Attributes file (JSON)
The attributes JSON file is created using the **AttributesToJSON** Nifi processor. How to add this to the standard-ingest template is mentioned below.

```javascript

{
    "spark.input_folder": "/tmp/kylo-nifi/spark",
    "uuid": "5dd38680-e3da-4ed7-80cf-3dc502ed924c",
    "dq.invalid.allowcnt":"0",
    "dq.invalid.pct":"0",
    "dq.active.rules":"SOURCE_TO_FEED_COUNT_RULE,ROW_COUNT_TOTAL_RULE",
    "feedts": "1488467463898",
    "kylo.tmp.baseFolder": "/tmp/kylo-nifi",
    "hive.ingest.root": "/model.db",
    "hive.master.root": "/app/warehouse",
    "metadata.table.fieldIndexString": "productCode,productName,productVendor,productDescription,buyPrice",
    "metadata.table.targetFormat": "STORED AS ORC",
    "source.record.count": "110",
    "merge.correlation": "toy_store.products.correlation",
    "skipHeader": "true",
    "source.row.count": "110",
    "metadata.table.partitionSpecs": "",
    "feed": "products",
    "hdfs.ingest.root": "/etl",
    "category": "toy_store",
    "absolute.hdfs.path": "/etl/toy_store/products/1488467463898",
    "metadata.table.feedFormat": "ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde' WITH SERDEPROPERTIES ( 'separatorChar' = ',' ,'escapeChar' = '\\\\' ,'quoteChar' = '\"') STORED AS TEXTFILE"
}
```
### Available Rules
This is the current set of available rules:
* **INVALID_ROW_PERCENT_RULE** - Ensures that the percentage of invalid rows out of all new rows is less than a set threshold
* **INVALID_ROW_TOTAL_COUNT_RULE** - Ensures that the total number of invalid rows is less than a set threshold
* **ROW_COUNT_TOTAL_RULE** - Ensures that the total number of source rows is equal to the total number of valid and invalid rows
 * **SOURCE_TO_FEED_COUNT_RULE** - Ensures that the total number of source rows is equal to the total number of feed rows

### Activate rules
To enable/disable a rule within a Nifi flow, update the **dq.active.rules** with the name of the rules that are active. This attribute is a comma-separated value with the name of the active rules.


### Adding a New Rule
To add a new rule, the following steps are required
1. Create a new implementation of DataQualityRule within package com.thinkbiganalytics.spark.dataquality.rule
2. Add the new rule within DataQualityChecker:setAvailableRules()
3. Build the code
4. Ensure that the new rule is added to the active rule attribute (dq.active.rules)

### Optional Nifi Attributes
These optional Nifi attributes are used by the current set of rules. Each has a default value if the attribute does not exist.
* **dq.invalid.allowcnt** - This sets the threshold for allowed number of invalid rows. The default is set to 0
  * ex: 10
* **dq.invalid.allowpct** - This sets the threshold for the allowed percentage of invalid rows. The default is set to 0
  * ex: 11
* **dq.active.rules** - This is a comma-separated value of all the active rules that will be executed. The current set of rules are provided above. By default, all rules are executed.
  * ex: INVALID_ROW_PERCENT_RULE,INVALID_ROW_TOTAL_COUNT_RULE,ROW_COUNT_TOTAL_RULE,SOURCE_TO_FEED_COUNT_RULE

### Adding Spark Job to standard-ingest
To add the Data Quality Job, the following changes need to be done to the standard-ingest template. These steps will done After the **Validate and Split Records** processor and before the **MergeTable** processor.
1. Add a **UpdateAttribute** processor which will provided configurable values for various rules. The list of attributes in the section above.
2. Add a **AttributesToJSON** processor which will convert the attributes to the JSON.
    1. Set the _Destination_ to flowfile-attribute
3. Add a **ExecuteScript** processor which will output the JSON attribute to a local file.
    1. Set the _Script Engine_ to Groovy
    2. Add the following script to the _Script Body_
```
    def flowFile = session.get()
      if(!flowFile) return
      def json = flowFile.getAttribute("JSONAttributes");
      def inputFolder = flowFile.getAttribute("spark.input_folder")
      def feed = flowFile.getAttribute("feed")
      def category = flowFile.getAttribute("category")
      def feedts = flowFile.getAttribute("feedts")
      def folder = new File(inputFolder + "/"+category+"/"+feed+"/"+feedts+"/"+"dq")
      // If it doesn't exist
      if( !folder.exists() ) {
      // Create all folders
      folder.mkdirs()
      }
      def jsonFile = new File(folder,feed+"_attributes.json")
      jsonFile.write(json)
      flowFile = session.putAttribute(flowFile,"attribute.json.path",jsonFile.getCanonicalPath())
      session.transfer(flowFile, REL_SUCCESS)
```
4. Add a **ExecuteSparkJob** processor which will execute the Spark job
    * Populate all the necessary Spark attributes
        * __ApplicationJar__ = /path/to/jar/kylo-spark-job-dataquality-spark-v1-<version>-jar-with-dependencies.jar
        * __MainClass__ =  com.thinkbiganalytics.spark.dataquality.checker.DataQualityChecke
        * __MainArgs__ to ${attribute.json.path}
