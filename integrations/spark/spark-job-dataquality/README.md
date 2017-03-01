Spark Data Quality Job
==========

### Overview
A Spark job capable of performing data quality checks as per provided rules.

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
    "spark.input_folder": "/tmp",
    "feedts": "1482206312228",
    "source.record.count": "10281",
    "source.row.count": "10281",
    "feed": "customer",
    "category": "foodmart"
}

```

### Adding a new rule

To add a new rule, the following steps are required
1. Create a new implementation of DataQualityRule within package com.thinkbiganalytics.spark.dataquality.rule
2. Add the new rule within DataQualityChecker:addRules()
3. Build the code


### Adding Spark Job to standard-ingest
To add the Data Quality Job, the following changes need to be done to the standard-ingest template. These steps will done After the **Validate and Split Records** processor and before the **MergeTable** processor.
1. Add a **AttributesToJSON** processor which will convert the attributes to the JSON.
    1. Set the _Destination_ to flowfile-attribute
2. Add a **ExecuteScript** processor which will output the JSON attribute to a local file.
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
3. Add a **ExecuteSparkJob** processor which will execute the Spark job
    1. Populate all the necessary Spark information (location of the jar, MainClass, etc)
    2. Set the __MainArgs__ to ${attribute.json.path}
