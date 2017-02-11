Service Level Agreement Jira Action
====
This module allows users to configure Service Level Agreements (SLA) to create JIRA issues when the SLA is violated.
To use this you need to include this jar in the /plugin directory (/opt/kylo/kylo-services/plugin) and configure the properties (jira.properties) to connect to your JIRA server.
As part of the default rpm build this module and the respective properties file is included with the build.

Email Properties
===
The system will look for properties starting with the prefix jira.
Below is an example properties file:
For more information on how to configure this refer to the git README in the [integrations/jira/jira-rest-client] project
```
### NOTE the java key store file (jira.keystorePath property) needs to be included using java keytool and on the classpath as defined below

#jira.host=your.jira.host.com
#jira.apiPath=/rest/api/latest/
#jira.username=USERNAME
#jira.password=PASSWORD
#jira.https=true
#jira.keystorePath=/your_jira_host_keystore.jks
#jira.keystorePassword=KEYSTORE_PASSWORD
#jira.keystoreOnClasspath=true
```

Other
====
For more information on how this plugin works please refer to the sla-core/README.md on how you can extend the SLA framework and create your own Actions when an SLA is violated.
