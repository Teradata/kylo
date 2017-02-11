Kylo Services Application
===
This is a Spring Boot application that will run a Tomcat server running as the service 'kylo-services'
This is the main backend application used by Kylo to communicate to the metadata database and NiFi.

NiFi Property Injection
====
Kylo is able to inject properties defined in the *application.properties* ('/src/main/resources/application.properties' or deployed at '/opt/kylo/kylo-services/conf/application.properties') file into the Nifi Flow
Kylo supports a variety of different strategies for injecting/replacing property values in the NiFi flow and NiFi controller services.

## Location of the properties files
The properties that get injected into the flows dont need to reside in the application.properties file.  Theyy can be externalized into separate properties files if you wish.
The properties are loaded by Spring and you can follow their convention to externalize the properties here, http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
Below is an example using Spring Boot profiles to load in these properties:
1. Name each file application-{profile}.properties
2. in the application.properties file add the profile names to the comma separated property *spring.profiles.active*

Example

1. Suppose there are 2 files that contain properties that are to be injected into the flows.  Put both of these on the classpath  (/opt/kylo/kylo/services/conf)
   - application-nifi1.properties
   - application-nifi2.properties
2. Update the application.properties to include the new profiles
```properties
spring.profiles.active=auth-kylo,auth-simple,nifi1,nifi2
``` 

## Property Value Encryption

Starting with version 0.4.2 of Kylo there is support for encrypting any property value in the Kylo property files.  In order to support encryption the java installation must be updated with the [Java Cryptographic Extensions](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html).  An encrypted property is identified by prepending the tag “{cipher}” to the encrypted value.  For example:

```
hive.datasource.password={cipher}29fcf1534a84700c68f5c79520ecf8911379c8b5ef4427a696d845cc809b4af0
```
   
### Encrypting a property value

By default, a new Kylo installation does not have any of its configuration properties encrypted.  Once you have started Kylo for the first time, the easiest way to derive encrypted versions of property values is to post values to the Kylo services ```/encrypt``` endpoint to have it generate an encrypted form for you.  You could then paste the encrypted value back into your properties file.  For instance, if you wanted to encrypt the Hive datasource password above (assuming the password is “mypassword”) you can get it’s encrypted form using curl like this:  

```
$ curl localhost:8420/encrypt –d mypassword
29fcf1534a84700c68f5c79520ecf8911379c8b5ef4427a696d845cc809b4af0
``` 

In this case, you would then copy that value and replace the clear text password string in the properties file with "{cipher}29fcf1534a84700c68f5c79520ecf8911379c8b5ef4427a696d845cc809b4af0".  

The benefit of this approach is that you will be getting a value that is guaranteed to work with the encryption settings of the server where that configuration value is being used.

## Processor Property Injection
   
###  Inject by matching the processor type name
Kylo can replace properties in processors matching on the Processor Type.  
The Processor Type is usually the default name NiFi gives the Processor and can be found by going to the processor settings tab under the heading "Type"
To use this strategy the property needs to match this structure:

```properties
###  PROCESSORTYPE and PROPERTY_KEY are all lowercase and the spaces are substituted with underscore
nifi.<PROCESSORTYPE>.<PROPERTY_KEY>=value
```   

*NOTE:* Using this strategy all processors of the given processorType in the flow will have the specified property replaced with the value defined in the properties file
Examples:

| Processor       | Property        | application.properties key     |
| --------------- | --------------- | ------------------------------ |
| GetFile         | Input Directory | nifi.getfile.input_directory   |
| PutFile         | Owner           | nifi.putfile.owner             | 
| ExecuteSparkJob | SparkHome       | nifi.executesparkjob.sparkhome |

   
###  Inject by matching the 'config.' prefix
Kylo can replace variables defined in your processor that start with the keyword 'config.'
When defining the flow in NiFi reference any of these variables as you would any other Nifi variable. ${config....}
To use this strategy just define your properties starting with the 'config.' prefix
Examples:

```properties
### application.properties 
config.hive.schema=hive
config.myproperty=some new property value 
``` 

With the properties defined above you can have a flow that has property values referencing ${config.hive.schema} and ${config.myproperty}.  When that flow is created/updated Kylo will replace those property values in the flow with the values found in the application.properties file 
 
*NOTE:* this keyword is configurable in the PropertyExpressionResolver Java class.
   

## Inject by matching property name replacing all properties in the flow
Kylo can replace all properties in a flow that match a given property name.
This strategy is useful if you have many common properties in a variety of processors in your flow that you want set to some common value.
A good example of this is the Kerberos values that have the same property in a variety of different processors.
To use this strategy the property needs to match this structure:

```properties
###  PROPERTY_KEY is all lowercase and the spaces are substituted with underscore
nifi.all_processors.<PROPERTY_KEY>=value
```   

Examples:

```properties
nifi.all_processors.kerberos_principal=nifi
nifi.all_processors.kerberos_keytab=/etc/security/keytabs/nifi.headless.keytab
nifi.all_processors.hadoop_configuration_resources=/etc/hadoop/conf/core-site.xml,/etc/hadoop/conf/hdfs-site.xml
```

*NOTE:*  This will globally replace all properties matching the defined property key!  Use this strategy wisely   
   
## Controller Service Property Injection
Kylo can replace controller service properties when they are first created from a reusable template or feed.
To do this the property needs to match this structure:

```properties
nifi.service.<NIFI_CONTROLLER_SERVICE_NAME>.<NIFI_PROPERTY_NAME>
```

| Controller Service         | Property                | application.properties key                               |
| -------------------------- | ----------------------- | ---------------------------------------------------------|
| Hive Thrift Service        | Database Connection Url | nifi.service.hive_thrift_service.database_connection_url |
| Kylo Metadata Service      | Rest Client Url         | nifi.service.kylo_metadata_service.rest_client_url       |

Examples:

```properties
nifi.service.hive_thrift_service.database_connection_url=jdbc:hive2://localhost:10000/default
nifi.service.kylo_metadata_service.rest_client_url=http://localhost:8400/proxy/v1/metadata
```

*NOTE:* Kylo will only do this replacement when the controller service is initially created by Kylo upon Feed or Reusable Template creation.  If the service already exists in NiFi then it will not do this replacement. 
