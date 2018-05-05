nifi-teradata-tdch-bundle
=========================

### Overview

This bundle provides NiFi controller services and processors that can be used to interact with Teradata using TDCH (Teradata Connector for Hadoop).


### Build
`[/nifi-teradata-tdch-bundle]$ mvn clean package`


### Deploy
`[/nifi-teradata-tdch-bundle]$ cp nifi-teradata-tdch-nar/target/kylo-nifi-teradata-tdch-nar-<version>.nar <nifi-installation-location>/lib`

**Example:**

`[/nifi-teradata-tdch-bundle]$ cp nifi-teradata-tdch-nar/target/kylo-nifi-teradata-tdch-nar-0.9.1-SNAPSHOT.nar /opt/nifi/current/lib`
 
- Restart NiFi after deploying .nar file