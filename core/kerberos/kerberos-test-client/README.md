Kerberos Test Client
====

## How to Use
java -jar kylo-kerberos-test-client-<version>.jar

## Example

~~~~
[root@sandbox opt]# /usr/lib/jvm/java/bin/java -jar /opt/kylo-kerberos-test-client-0.4.2-SNAPSHOT.jar

Please enter the list of configuration resources:  /etc/hadoop/conf/core-site.xml,/etc/hadoop/conf/hdfs-site.xml

Please enter the keytab file location: /etc/security/keytabs/kylo.headless.keytab

Please enter the principal name: thinkbig@sandbox.hortonworks.com

Executing Kinit to generate a kerberos ticket
log4j:WARN No appenders could be found for logger (org.apache.hadoop.metrics2.lib.MutableMetricsFactory).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
Java config name: /etc/krb5.conf
Loaded from Java config
Generating Kerberos ticket for principal: thinkbig@sandbox.hortonworks.com at key tab location: /etc/security/keytabs/thinkbig.headless.keytab
Java config name: /etc/krb5.conf
Loaded from Java config
>>> KdcAccessibility: reset
>>> KdcAccessibility: reset
>>> KeyTabInputStream, readName(): sandbox.hortonworks.com
>>> KeyTabInputStream, readName(): thinkbig
>>> KeyTab: load() entry length: 82; type: 18
>>> KeyTabInputStream, readName(): sandbox.hortonworks.com
>>> KeyTabInputStream, readName(): thinkbig
>>> KeyTab: load() entry length: 66; type: 17
>>> KeyTabInputStream, readName(): sandbox.hortonworks.com
>>> KeyTabInputStream, readName(): thinkbig
>>> KeyTab: load() entry length: 74; type: 16
>>> KeyTabInputStream, readName(): sandbox.hortonworks.com
>>> KeyTabInputStream, readName(): thinkbig
>>> KeyTab: load() entry length: 66; type: 23
Added key: 23version: 1
Added key: 16version: 1
Added key: 17version: 1
Added key: 18version: 1
Ordering keys wrt default_tkt_enctypes list
default etypes for default_tkt_enctypes: 23 18 17.
Added key: 23version: 1
Added key: 16version: 1
Added key: 17version: 1
Added key: 18version: 1
Ordering keys wrt default_tkt_enctypes list
default etypes for default_tkt_enctypes: 23 18 17.
default etypes for default_tkt_enctypes: 23 18 17.
>>> KrbAsReq creating message
>>> KrbKdcReq send: kdc=sandbox.hortonworks.com UDP:88, timeout=30000, number of retries =3, #bytes=167
>>> KDCCommunication: kdc=sandbox.hortonworks.com UDP:88, timeout=30000,Attempt =1, #bytes=167
>>> KrbKdcReq send: #bytes read=672
>>> KdcAccessibility: remove sandbox.hortonworks.com
Added key: 23version: 1
Added key: 16version: 1
Added key: 17version: 1
Added key: 18version: 1
Ordering keys wrt default_tkt_enctypes list
default etypes for default_tkt_enctypes: 23 18 17.
>>> EType: sun.security.krb5.internal.crypto.ArcFourHmacEType
>>> KrbAsRep cons in KrbAsReq.getReply thinkbig

Sucessfully got a kerberos ticket in the JVM

~~~~
