Core
==========

### Overview

The subprojects in this subdirectory are core API frameworks that can generally used by developers to extend the capabilities of the Think Big accelerator platform 

Each project may have an additional README file and JavaDoc.  You may find default implementations and deployment instructions in the [plugins](../plugins) subtree.

### Subproject Overview

#### Job Repositories

A job repository provides the status of jobs and feeds

| Project        | Description           |
| ------------- |-------------|
| [alerts](alerts) | APIs for raising alerts in Pipeline Controller dashboard and to develop potential integrations with external notification services (email, JIRA, Nagios, etc.)
| [field-policy](field-policy) | Framework for developing custom standardization and validation policies
| [job-repository](job-repository) | APIs for recording job metadata used to develop support for different job execution engine (e.g. Spring Batch or NiFi) 
| [schema-discovery](schema-discovery) | Infers a target schema from sample file or database
| [service-monitor](service-monitor) | Framework for developing custom service monitoring for visibility in Pipeline Controller
| [sla](sla) | Framework for developing service level agreements to be enforced by Think Big service 
| [calendar](calendar) | APIs for developing a Calendar for use in Service Level Agreements