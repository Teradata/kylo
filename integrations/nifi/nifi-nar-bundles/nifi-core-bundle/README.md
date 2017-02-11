nifi-core-bundle
==========

### Overview

The NiFi core bundle is a NiFi extension providing standard ingest components and integrations with the metadata server

### Component Descriptions


#### Ingest Support

| Component        | Description           |
| ------------- |-------------|
| MergeTable | Merges data from a source table and partition into the target table (inserting and adding)
| GetTableData | Polls a database table with the ability to do snapshots or incremental fetch with high-water recording
| RegisterFeedTables | Creates standard tables in support of ingest workflow. This is referred to as "table registration"
| RouteOnRegistration | Routes through an alternate flow path based on the status of table registration
| UpdateRegistration | Updates state of a feed in metadata repository to note that table registration has been performed

#### Ingest Support

| Component        | Description           |
| ------------- |-------------|
| BeginFeed | Denotes the start of a feed with capability to receive JMS messages to trigger the flow. 
| TerminateDirectoryFeed | Denotes the termination of a feed that resulted in data written to a directory. Evicts a JMS event based on the data change.
| TerminateHiveTableFeed | Denotes the termination of a feed that resulted in data written to a Hive take. Evicts a JMS event based on the data change.

### Dependencies

Use of this bundle requires the nifi-hadoop-services-nar