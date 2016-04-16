Thinkbig Data Lake Server
=========
The Data Lake Server Project has modules that preform distinct backend services and supports serving up this content via a REST endpoint for various clients to interact with.
 
Structure
=========
The server has a spring boot application project along with various modules that do indpendent server side work.
These modules also serve up a REST endpoint allowing various clients to interact with them.
The Server is designed to be configurable at build time allowing you to pick and choose various modules/sub modules. 
Depending on your maven profile it can wire together specific pieces tailored to your needs.  
For example suppose you wish to create a server that is using Ambari and you want to monitor ambari services
You can build this project with the *ambari* profile to include just that resource

***NOTE***: *The maven profiling is a work in progress*

  
Modules
=========

1. data-lake-server-app

The Server application is a Spring Boot application.  

1. job-repository

Responsible for Reading and Writing Job Execution data.  
Currently we support MySQL and Postgres as backends to store this data.  Under the covers we are using the JSR-352 compliant implementation using Spring Batches Data Model.
The Execution Engine that is preforming these jobs is separate from the actual storage mechanism.  The default implementation for the data-lake-accelerator is Apache Nifi.  This is in the sub-module
*job-repository-nifi*
Alternatively if you wish you can use Spring Batch as the Execution Engine.  These files still exist in the *InternalAssets* project and will be ported over from the *pipeline-components* module

1. scheduler

Responsible for displaying the schedule information along with a REST endpoint to allow for user interaction with the schedule (start, pause etc)

1. service-monitor

Responsible for exposing various Service Health checks as a REST endpoint.  The ***data-lake-ui/operations-manager*** knows how to interact with these Health Checks and displays this information on the UI.
The service-monitor is pluggable allowing you to easily add in health checks to any service and expose that information in the operations manager user interface.