## Example Module

This project showcases how you can create custom Java code for kylo-services [example-module-services](example-module-services) and custom angular ui code for kylo-ui [example-module-ui](example-module-ui)  
 - the kylo-service Java code is a Java jar that is placed in the /opt/kylo/kylo-services/plugin folder
 - the kylo-ui code is a jar with angular/html code that is placed in the /opt/kylo/kylo-ui/plugin folder
 
### Code Structure

   -  [example-module-services](example-module-services) | Custom kylo-service code with a example REST controller and adds a new permissions into Kylo
   -  [example-module-ui](example-module-ui) | Custom angular module that creates new side navigation using the permission above and calls the custom REST controller
    
    