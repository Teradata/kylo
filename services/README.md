Kylo Server
=========
The Kylo Server has modules that preform distinct backend services and supports serving up this content via a REST endpoint for various clients to interact with.
 
Structure
=========
The server has a spring boot application project along with various modules that do independent server side work.
These modules also serve up a REST endpoint allowing various clients to interact with them.
  
Modules
=========

### server-app
The Server application is a Spring Boot application.    

Swagger is used to document all rest controllers.  The swagger path is /api-docs/index.html

### scheduler
Responsible for displaying the schedule information along with a REST endpoint to allow for user interaction with the schedule (start, pause etc)  
Currently this is used to schedule Service Level Agreements in Kylo.
Feed scheduling is done using Apache NiFi.

### service-monitor
Responsible for exposing various Service Health checks as a REST endpoint for the [ui/ui-ops-mgr]
The service-monitor is pluggable allowing you to easily add in health checks to any service and expose that information in the operations manager user interface.  

### thrift-proxy
Proxy to Hive JDBC and Metadata store
