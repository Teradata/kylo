Feed Wizard Plugins
===================

Overview
--------

Additional steps can be added to the Create Feed and Feed Details pages with a Feed Wizard plugin. A plugin will have access to the feed's metadata to add or modify properties.

Two plugins are included with Kylo: Data Ingest and Data Transformation. The Data Ingest plugin adds steps to define a destination Hive table. The Data Transformation plugin adds steps to generate a
Spark script that transforms the data. Both of these plugins can be used as examples.

There are a seres of 4 different plugin configurations in this project that setup various feed steppers.

Plugin Definition
-----------------

A simple stepper in Kylo is defined with 5 default steps
1. General Info  - general info about the feed (name, description)
2. Feed Details - NiFi processor properties
3. Properties  - Additional business properties (tags, owner)
4. Access Control - if entity access is enabled.
5. Schedule - the schedule of the feed

The plugin should provide a JSON file describing its purpose and indicates what templates it uses.

**NOTE**:  This json file must end with the suffix `-stepper-definition.json`

 - The metadata properties refer to `model.tableOption` in the templates and will be automatically prefixed with `metadata.tableOption`.
 - You can two types of steps
    1. pre-steps.  These will be rendered prior to the `General Info` step section.  These are useful if you want a form that users fill out that will validate before they create their feeds.  These steps are defined with the `preStepperTemplateUrl` and `totalPreSteps` properties
    2. core-steps.  These will be rendered after the `Feed Details` step. These are defined with the `stepperTemplateUrl` and `totalCoreSteps` properties

```json

{
  "description": "A human-readable summary of this option. This is displayed as the hint when registering a template (Required) ",
  "displayName": "A human-readable title of this option. This is displayed as the title of this option when registering a template. (Required)  ",
  "resourceContext":"The url prefix/directory where these resources are located.  Example:  /example-plugin-1.0  (Required)",
  "feedDetailsTemplateUrl": "The location (with the /resoureceContext) of the html when viewing/editing a feed. (Required if you define the 'stepperTemplateUrl' property)",
  "stepperTemplateUrl": "The location (with the /resourceContext) of the html for creating a new feed (the feed steps).  This does not include any pre-steps (Either 'preStepperTemplateUrl' or this property is required)",
  "preStepperTemplateUrl":"The location (with the /resourceContext) of the html for any pre-steps  (Either 'stepperTemplateUrl' or this property is required) ",
  "preFeedDetailsTemplateUrl":"The location (with the /resourceContext) of the html when viewing/editing a feed for any pre-steps.  (Optional)",
  "metadataProperties": [
    {
      "name": "A property name",
      "description": "A description of the property"
    }   
  ]  ,
  "totalCoreSteps": "A number datatype. The number of steps defined in the 'stepperTemplateUrl'  (does not include pre-steps) (Required only if 'stepperTemplateUrl' property is defined)",
  "totalPreSteps": "A number datatype. The number of steps defined in the 'preStepperTemplateUrl'  (does not include core-steps),  (Required only if 'preStepperTemplateUrl' property is defined)",
  "type": "Unique identifier for this stepper/template type",
  "initializeServiceName":"The name of the angular Initialization Service to call, defined in the 'initializeScript' file.  (Required if you define the 'initializeScript' property) ",
  "initializeScript": "The location (with the /resourceContext) of the initialization angular service.  See Initialization Service section below.  (Optional)"
}
````

Stepper Templates
-----------------

The Stepper template should add additional steps, if any, to the Create Feed Wizard. Each step is defined by a `<kylo-define-feed-step>` which contains the HTML to be displayed for that step.

```html
<kylo-define-feed-step title="Example" step="getStep(0)">
  <div oc-lazy-load="['/example-ui-feed-stepper-plugin-1.0/ExampleUiFeedStepperCard.js']">
    <example-ui-feed-stepper-card step-index="{{getStepIndex(0)}}" ng-if="isStepSelected(0)"></example-ui-feed-stepper-card>
  </div>
</kylo-define-feed-step>
```

Helper functions are provided for interacting with the stepper. Each function takes the index starting with 0 for the step within the plugin.

Function | Returns | Description
-------- | ------- | -----------
getStep(number) | Object | Gets the object for the table option step at the specified index.
getStepIndex(number) | number | Gets the stepper step index for the specified table option step index.
isStepSelected(number) | boolean | Indicates if the specified step is selected.
isStepVisited(number) | boolean | Indicates if the specified step has been visited.

Feed Details Template
---------------------

The Feed Details template should add additional sections, if any, to the edit feed page. Each section is typically a new directive with its own `<vertical-section-layout>`.


Initialization Service
----------------------

You can define an optional Angular Initialization service that will be called when the feed creation stepper or edit feed is rendered.
This allows you to setup your feed and set default options, show/hide fields, steps etc.
the service must follow the following constructs.  (see the 'initialize.js' for an example)
 - Must be an angular factory or service
 - Must have 2 public methods:
    - initializeCreateFeed(tableOptionsMetadata, feedStepper).  
     This takes 2 arguments. 
       - tableOptionsMetadata  - This is the metadata defined in this file
       - feedStepper  - This is the instance of the feedStepper controller  (see /common/stepper.js).  You can activate/deactivate steps
    - initializeEditFeed(tableOptionsMetadata).  This takes 1 argument
       - tableOptionsMetadata  - This is the metadata defined in this file

Refer to the `initialize.js` for a complete example

**NOTE**  This Angular Service needs to belong to the `kylo.feedmgr.feeds` angular module, not the module for this specific plugin.  This is because that is the module that will be launching and running it.
 
 The example below registers the Service using the `moduleName` coming from the file `feed-mgr/feeds/module-name` which resolves to: `kylo.feedmgr.feeds` 
 
 ```javascript
 
    define(["angular", "feed-mgr/feeds/module-name"], function (angular, moduleName) {
        
        var service = function(FeedService, StepperService){
            
             var data = {
                        initializeCreateFeed:function(optionsMetadata,feedStepper, feedModel){
                            //init create feed
                          },
                         initializeEditFeed:function(optionsMetadata,feedModel) {
                            //init edit feed
                         }
                    }
                    return data;
        }
        
         angular.module(moduleName)
                .factory("ExampleFeedStepperInitializerService", ["FeedService","StepperService", service])
    }
```

