# Think Big Data Lake Accelerator

The latest generation of Pipeline Controller represents a major revision to our Data Lake 
assets.  Some features:

    * Apache NiFi engine (vs. Spring Batch)
    * Apache Spark for data ingest processing  
    * Additional UI module oriented to Data Lake users
    * Users can create "data feeds" through our UI (no-code!)
    * Powerful ingest pipeline template that implements Think Big best practices

## Wiki

[R&D Wiki](https://wiki.thinkbiganalytics.com/display/RD/Pipeline+Controller+-+Next+Generation/)

## Deployment Guide

To get started, please follow the [deployment guide](docs/latest/deployment-guide.adoc)

## Code Structure

The layout of code in the following subtrees follows particular reasoning described below: 

| Subfolder        | Description           |
| ------------- |-------------|
| [commons](commons) |  Utility or common functionality used through the Think Big accelerator platform
| [core](core) | API frameworks that can generally used by developers to extend the capabilities of the Think Big accelerator platform
| [docs](docs) | Documentation that should be distributed as part of release
| [integrations](integrations) | Pure integration projects with 3rd party software such as NiFi and Spark. 
| [metadata](metadata) | The metadata server is a top-level project for providing a metadata repository, REST API for the recording of metadata
| [plugins](plugins) | Alternative and often optional implementations for operating with different distributions or technology branches
| [security](security) | Support for application security for both authentication and authorization
| [services](services) | Provides REST endpoints and core server-side processing 
