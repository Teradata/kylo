Core
==========

### Overview

The subprojects in this subdirectory are core API frameworks that can generally used by developers to extend the capabilities of Kylo. 

Each project may have an additional README file and JavaDoc.  You may find default implementations and deployment instructions in the [plugins](../plugins) subtree.

### Subproject Overview

#### Job Repositories

A job repository provides the status of jobs and feeds

| Project        | Description           |
| ------------- |-------------|
| [alerts](alerts) | APIs for raising alerts and to develop potential integrations with external notification services (email, JIRA, Nagios, etc.)
| [field-policy](field-policy) | Framework for developing custom standardization and validation policies
| [job-repository](job-repository) | APIs for recording job metadata 
| [schema-discovery](schema-discovery) | Infers a target schema from sample file or database
| [service-monitor](service-monitor) | Framework for developing custom service monitoring for visibility in Kylo
| [sla](sla) | Framework for developing service level agreements
| [ui-annotation](ui-annotation) | Annotation framework for exposing Java classes to the user interface.  Used by SLA's, field-policy,pre-conditions, schema-discovery