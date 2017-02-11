Kylo Service Monitor
==============

This module allows for the ui-operations-manager to monitor any number of services and verify if they are up/down/healthy, etc.

Setup
-------------

The ***service-monitor-controller*** looks for any class on the classpath that implements one of two certain interface.

| Interface        | Description           | Example Impl |
| --------- | --------- | ----------- |
| 1. `com.thinkbiganalytics.servicemonitor.check.ServiceStatusCheck` | Check and return status for a single service | `com.thinkbiganalytics.servicemonitor.check.PipelineDatabaseServiceStatusCheck` |
| 2. `com.thinkbiganalytics.servicemonitor.check.ServicesStatusCheck` | Check and return status for a multiple services | `com.thinkbiganalytics.servicemonitor.check.AmbariServicesStatusCheck` |

Any class on the classpath that implements one of those interfaces above will have the service monitoring displayed in the Operations Manager UI

How To Use
--------------

1. Create a new maven jar project/module  
2. add the ***service-monitor-api*** and ***service-monitor-core*** maven dependencies   
```xml
        <dependency>
            <groupId>com.thinkbiganalytics</groupId>
            <artifactId>kylo-service-monitor-api</artifactId>
            <version>0.7.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>com.thinkbiganalytics</groupId>
            <artifactId>kylo-service-monitor-core</artifactId>
            <version>0.7.0-SNAPSHOT</version>
        </dependency>
```  

3. Create a class that implements one of the `ServiceStatus(s)Check` interfaces  
```java
public class MySystemStatusCheck implements ServiceStatusCheck {

  @Override
  public ServiceStatusResponse healthCheck() {

    String serviceName = "MySystem";
    //A service can comprise of 1 or more components
    //Each component has its on set of properties and health information
    //Build the Component(s) on this Service that you wish to monitor
    //Call out to a url, database, shell script, etc, to obtain the necessary Health information
    ServiceComponent component = new DefaultServiceComponent.Builder("MySystem Component", ServiceComponent.STATE.UP).message(
        "MySystem is up.").build();
    //Create the Overall Service Response from the set of Components
    ServiceStatusResponse response = new DefaultServiceStatusResponse(serviceName, Arrays.asList(component));
    return response;
  }
}
```  

