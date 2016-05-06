nifi-hadoop-bundle
==========

### Overview

The NiFi Hadoop bundle provides general Hadoop extensions.  These extensions may be available eventually via vanilla NiFi 

### Component Descriptions

| Component        | Description           |
| ------------- |-------------|
| ExecuteHQL | Execute provided HQL via Thrift server to Hive or Spark. Query result will be converted to Avro format
| ExecuteHQLStatement | Execute provided HIVE or Spark statement. This can be any HQL DML or DDL statement that results in no results.


### Dependencies

Use of this bundle requires the nifi-hadoop-services-nar