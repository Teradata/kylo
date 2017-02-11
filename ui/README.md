User Interface Project
======
This is the parent project for all UI related material.
The following submodules are included

### ui-app
  This is the Spring Boot application that will run the different modules/webapps.  Currently there are 2 webapp modules.  Each module is included in this spring boot app by adding the respective maven dependency.
  
### ui-common
  This stores all common vendor code and common angular directives and JavaScript files
  
### ui-feed-manager
  This module integrates with Apache Nifi to allow you to configure feeds 
  
### ui-operations-manager
  View operations, running jobs and execution data
  