Hadoop Ranger Authorization Plugin
====
This module allows you to configure Kylo to query groups from Ranger when creating a new category and a new feed. If you
assign Ranger groups at the category level, the feed level groups will be defaulted with those values. You will have the option to modify the groups
as part of the feed creation process

In addition there is a Ranger NiFi processor that can create Ranger policies based on the needs of your template. You can
see an example of this in the data ingest sample template.

To use this you need to include this jar in the /plugin directory (/opt/thinkbig/thinkbig-services/plugin) and configure the properties
(authorization.ranger.properties) to connect to your Ranger.

This plugin is not installed by default as part of the RPM install

Ranger Properties
===
Below is an example properties file:

```
ranger.hostName=localhost
ranger.port=6080
ranger.userName=admin
ranger.password=admin
```

What is Not Supported in This Release?
For the 0.4.0 release we only added support for creating Ranger policies. In a follow up release we will add support for removing policies when
deleting a feed, and modifying groups for a feed.