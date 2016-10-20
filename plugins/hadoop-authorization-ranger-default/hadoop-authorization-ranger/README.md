Hadoop Ranger Authorization Plugin
====
This module allows you to configure Kylo to query groups from Ranger when creating a new category and a new feed. If you
assign Ranger groups at the category level, the feed level groups will be defaulted with those values. You will have the option to modify the groups
as part of the feed creation process

In addition there is a Ranger NiFi processor that can create Ranger policies based on the needs of your template. You can
see an example of this in the data ingest sample template.

To use this you need to do the following
* Include this jar in the /opt/thinkbig/thinkbig-services/plugin folder
* Copy the authorization.ranger.properties to the /opt/thinkbig/thinkbig-services/conf folder
* Configure the authorization.ranger.properties file

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

Development
===
To test the plugin in your IDE you need to add the below two maven modules to the thinkbig-services app

* hadoop-authorization-ranger
* ranger-rest-client