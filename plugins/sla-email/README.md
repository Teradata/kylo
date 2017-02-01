Service Level Agreement Email Action
====
This module allows users to configure Service Level Agreements (SLA) to send emails when the SLA is violated.
To use this you need to include this jar in the /plugin directory (/opt/kylo/kylo-services/plugin) and configure the properties (sla.email.properties) to connect to your mail server.
As part of the default rpm build this module and the respective properties file is included with the build.

Email Properties
===
The system will look for properties starting with the prefix sla.mail
Below is an example properties file:

```
## if sla-email jar is a configured plugin configure the Email connection
##uncommenet the settings below for Gmail to work
sla.mail.protocol=smtp
sla.mail.host=smtp.google.com
sla.mail.port=587
sla.mail.smtpAuth=true
sla.mail.starttls=true
####Note gmail will not respect the from address due to their security restraints, but other systems will
sla.mail.from=sla-violation@thinkbiganalytics.com
sla.mail.username=<USERNAME>
sla.mail.password=<PASSWORD>
####additional options (not needed for gmail, but for other servers)
##sla.mail.sslEnable=true
##sla.mail.smptAuthNtmlDomain=<SOME_DOMAIN>
```

Other
====
For more information on how this plugin works please refer to the sla-core/README.md on how you can extend the SLA framework and create your own Actions when an SLA is violated.
