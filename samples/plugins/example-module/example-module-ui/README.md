## Example UI Module

Kylo allows you to code your own custom angular modules to create custom pages 

### The module-definition.json 

1. Create the root user interface folder in your project "/src/main/java/resources/static/js/plugin".  This will be where your user interface code will go.
2. Create sub folders for your various angular modules you want to code and include into this plugin. 
3. Inside your module folder (i.e. 'example-module'), create a JSON file with the suffix "module-definition.json".  Usually its good to prefix this file with the name of the module.  This file describes the metadata about the module.

**Note**: Kylo uses the "angular-ui-router" (https://ui-router.github.io/ng1/docs/latest/).  The 'states' array below just needs to be the metadata describing the state.  Only the 3 items below are needed.  Your code will be 
```json
{
  "moduleName": "String - optional String for the module name",
  "moduleJsUrl":"String - location of the module.js. (without the /js prefix and .js suffix.  Example: the file in /js/plugin/example-module/module.js  should be   plugincollapsibledule/module ",
  "states": [  
    {
      "state": "String - unique name for your state.  This is used for navigation to your page/module",
      "url": "String - url for your page. ",
      "params": "Map - optional parameters that are passed into your state in the url"
    }
  ],
  "navigation": [
    {
      "toggleGroupName": "String - if making a new collapsible section give this a unique name, otherwise reference one of the following to place your module in the existing sections: 'OPS_MGR, 'FEED_MGR','ADMIN'",
      "text": "String - the name of the parent collapsible section.  Ths is only needed if this is a new module.  If you referenced an existing module name (i.e. 'OPS_MGR, 'FEED_MGR','ADMIN') then you dont need this property",
      "narrowText": "String - the abbreviated name of the parent collapsible section when shrunk left.  Ths is only needed if this is a new module.  If you referenced an existing module name (i.e. 'OPS_MGR, 'FEED_MGR','ADMIN') then you dont need this property",
      "links": [
        {
          "sref": "String - The name of the 'state' for the link.",
          "icon": "String - an icon for the link.  Refer to https://klarsys.github.io/angular-material-icons/ for icon names.",
          "text": "String - the text of the link",
          "permission": ["An array of Service permissions that restrict access to this page.  Leave as an empty array or null to allow all access"]
        }
      ]
    }
  ]
}
```


An example JSON file is here:

```json
{
  "moduleName": "example",
  "moduleJsUrl":"plugin/example-module/module",
  "states": [
    {
      "state": "exampleModule",
      "url": "/exampleModule",
      "params": {
        "name": null
      }
    }
  ],
  "navigation": [
    {
      "toggleGroupName": "NEW_MODULE",
      "text": "New Module",
      "narrowText": "New",
      "links": [
        {
          "sref": "exampleModule",
          "icon": "transform",
          "text": "Example",
          "permission": []
        }
      ]
    }
  ]
}
```

### Angular code

 - Each module should have a 'module.js' file that defines the ui-router states.
 - Refer to the [module.js](src/main/resources/static/js/plugin/example-module/module.js)
 - You can get access to Kylo Angular Services/directives by including the appropriate dependency in the module.js
  
  ```javascript
  define[angular,'module names to include here']
  ```

- Adding **'kylo-common'**  allows you to reference/inject all of the services in: [kylo-common](../../../../ui/ui-app/src/main/resources/static/js/common)
- Adding **'kylo-services'**  allows you to reference/inject all of the services in: [kylo-services](../../../../ui/ui-app/src/main/resources/static/js/services)
- Adding **'kylo-opsmgr'**  allows you to reference/inject all of the services in: [kylo-opsmgr](../../../../ui/ui-app/src/main/resources/static/js/ops-mgr)
- Adding **'kylo-feedmgr'**  allows you to reference/inject all of the services in: [kylo-feedmgr](../../../../ui/ui-app/src/main/resources/static/js/feed-mgr)

Example include

 ```javascript
  define[angular,'kylo-common','kylo-opsmgr']
  ```
  
 - Kylo uses this same model in its own code. You can refer to [internal kylo angular code](../../../../ui/ui-app/src/main/resources/static/js) for more examples 