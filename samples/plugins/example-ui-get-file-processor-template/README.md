Custom Processor Templates
Kylo allows you to code your own custom angular template for a NiFi Processor

The Java ``ProcessorTemplate`` interface
1. Create a new Java Class that implements the ProcessorTemplate interface

 - The getProcessorTypes() needs to return an Array of the fully qualified NiFi Processor class name(s)
 - The getStepperUrl() needs to return the html file to be used when creating the Feed.  Return null to have the system use its default rendering
 - The getFeedDetailsTemplateUrl() needs to return the html file to be used when editing a feed.  Return null to have the system use its default rendering

```java

@Component
public class GetFileTemplate implements ProcessorTemplate {

    @Override
    public List getProcessorTypes() {
        return Lists.newArrayList("org.apache.nifi.processors.standard.GetFile");
    }

    @Nullable
    @Override
    public String getStepperTemplateUrl() {
        return "js/plugin/processor-templates/GetFile/get-file-processor.html";
    }

    @Nullable
    @Override
    public String getFeedDetailsTemplateUrl() {
        return "js/plugin/processor-templates/GetFile/get-file-processor.html";
    }
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
      "properties": [ <property.json>', ...],
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
      "value": "mydata\\d{1, 3}.csvfffg",
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