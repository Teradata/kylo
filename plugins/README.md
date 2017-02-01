Plugins
==========

### Overview

The subprojects in this subdirectory are alternative and often optional implementations for operating with different distributions or technology branches

#### Deployment Process

RPM and installation procedure should install selected JAR file(s) and dependencies to /opt/kylo/kylo-services/plugin folder

Other setup may be required based on the individual requirements of the project

### Subproject Overview

#### Job Repositories

A job repository provides the status of jobs and feeds

| Project        | Description           |
| ------------- |-------------|
| [job-repository-nifi](job-repository-nifi) | Support for recording NiFi-based jobs in the job repository 
| [job-repository-spring-batch](job-repository-spring-batch) | Support for recording Spring-based jobs in the jobs repository


#### Job Repositories

| Project        | Description           |
| ------------- |-------------|
| [service-monitor-ambari](service-monitor-ambari) | Provides a service monitoring hook of Hortonworks Hadoop cluster using Ambari APIs
| [service-monitor-cloudera](service-monitor-cloudera) | Provides a service monitoring hook of Cloudera cluster using CDH APIs
| [service-monitor-nifi](service-nifi) | Provides service monitoring of Apache NiFi
| [service-monitor-pipeline-db](service-monitor-pipeline-db) | Provides monitoring of a database 


