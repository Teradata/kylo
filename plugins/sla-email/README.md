# Service Level Agreement Email Action

This module allows users to configure Service Level Agreements (SLA) to send emails when the SLA is violated.

## Setup

Add plugin jar `kylo/plugins/sla-email` from Kylo repository to
```
$KYLO-HOME/kylo-services/plugin
```

Configure file
```
$KYLO-HOME/kylo-services/conf/sla.email.properties
```

## Email Properties

The system will look for properties starting with the prefix `sla.mail`

Below is an example properties file (for Gmail):

```
sla.mail.protocol=smtp
sla.mail.host=smtp.gmail.com
sla.mail.port=587
sla.mail.smtpAuth=true
sla.mail.starttls=true

# Note Gmail will not respect the from address due to their security restraints, but other systems will
# Note Outlook will not send an email if the from address is not a valid outlook email (can be the username email)
# Default sla.mail.from=sla.mail.username
sla.mail.from=sla-violation@thinkbiganalytics.com

sla.mail.username=<USERNAME>
sla.mail.password=<PASSWORD>

# Additional options (not needed for gmail, but for other servers)
# sla.mail.sslEnable=true
# sla.mail.smptAuthNtmlDomain=<SOME_DOMAIN>
```

## Other

For more information on how this plugin works please refer to the [core/sla/README.md](../../core/sla/README.md) on how you can extend the SLA framework and create your own Actions when an SLA is violated.
