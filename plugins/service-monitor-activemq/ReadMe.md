Activemq Service Monitor Plugin
====
This module allows you to monitor Activemq service and status is show on Kylo UI under Service tab. Activemq URL is obtained from Kylo configuration file. 

Pre-requisite : Make sure jms.activemq.broker.url is configured in kylo-services/conf/activemq.properties.

To use this you need to do the following

* Copy jar from /opt/kylo/setup/plugins/kylo-service-monitor-activemq-<Kylo-Version>.jar to the /opt/kylo/kylo-services/plugin/kylo-service-monitor-activemq-<Kylo-Version>.jar  folder
* Make sure jar file is owned by Kylo service user.
* Restart Kylo Services

```
service kylo-services stop
service kylo-services start
```

