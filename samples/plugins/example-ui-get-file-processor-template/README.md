Custom Processor Templates
Kylo allows you to code your own custom angular template for a NiFi Processor

1. Create a JSON file with the suffix "processor-template-definition.json".  Usually its good to prefix this file with the name of the processor (i.e. "get-file-processor-template-definition.json")

```json
{
  "processorDisplayName":"An optional String to have the code Match on the processorType and the label of the processor.  If Null or not set it will just use the processorType to match when rendering the template",
  "processorTypes":[ "Array of the fully qualified NiFi Processor class name(s)"],
  "stepperTemplateUrl":"the html file to be used when creating the Feed.  Return null to have the system use its default rendering",
  "feedDetailsTemplateUrl":"the html file to be used when editing a feed.  Return null to have the system use its default rendering"
}
```


An example JSON file is here:

```json
{
  "processorTypes":["org.apache.nifi.processors.standard.GetFile"],
  "stepperTemplateUrl":"js/plugin/processor-templates/GetFile/get-file-processor.html",
  "feedDetailsTemplateUrl":"js/plugin/processor-templates/GetFile/get-file-processor.html"
}
```


2. The angular code
 - Each template is provided 2 objects in its scope
   - processor:   the current processor with its properties (see example JSON below)
   - theForm:   the parent html form used for adding validation rules

 - When rendering you can use the directive 'nifi-property-input' if you want the fallback to the system rendering the property. 
   Otherwise you can choose to customize the template and render the processor/properties any way you please.
   
 - Example JSON for Angular 
   - Processor JSON
    ```json
    {
      "allProperties": ["<property.json>, ... All properties for the processor"],
      "properties": [ "<property.json>, ... Only those properties Registered for 'user input'.  Typically these are only the ones displayed for the end user"],
      "type": "org.apache.nifi.processors.standard.GetFile",
      "name": "Filesystem",
      "inputProcessor": true,
      "userEditable": true,
      "feedPropertiesUrl": "js/plugin/processor-templates/GetFile/get-file-processor.html"
    }
    
    ```    
    
     - Example Property JSON
     ```json
    {
      "processorName": "Filesystem",
      "processorType": "org.apache.nifi.processors.standard.GetFile",
      "key": "File Filter",
      "value": "mydata\\d{1, 3}.csv",
      "expressionProperties": null,
      "propertyDescriptor": {
        "name": "File Filter",
        "displayName": null,
        "description": null,
        "defaultValue": null,
        "allowableValues": null,
        "required": null,
        "sensitive": null,
        "dynamic": null,
        "supportsEl": null,
        "identifiesControllerService": null
      },
      "templateValue": "mydata\\d{1, 3}.csv",
      "userEditable": true,
      "renderType": "text",
      "renderOptions": {},
      "selected": false,
      "inputProperty": true,
      "containsConfigurationVariables": false,
      "sensitive": false,
      "required": false,
      "renderWithCodeMirror": false,
      "displayValue": "mydata\\d{1, 3}.csv",
      "formKey": "file_filterfilesystem"
    }
     ```