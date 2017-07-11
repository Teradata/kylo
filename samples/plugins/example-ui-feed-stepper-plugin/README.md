Feed Wizard Plugins
===================

Overview
--------

Additional steps can be added to the Create Feed and Feed Details pages with a Feed Wizard plugin. A plugin will have access to the feed's metadata to add or modify properties.

Two plugins are included with Kylo: Data Ingest and Data Transformation. The Data Ingest plugin adds steps to define a destination Hive table. The Data Transformation plugin adds steps to generate a
Spark script that transforms the data. Both of these plugins can be used as examples.

Plugin Definition
-----------------

The plugin should provide a Java bean that describes its purpose and indicates what templates it uses. A built-in REST API makes the bean available to JavaScript which loads the template URLs when
requested.

The metadata properties refer to `model.tableOption` in the templates and will be automatically prefixed with `metadata.tableOption`.

```java
import com.thinkbiganalytics.ui.api.template.TemplateTableOption;

public class ExampleFeedStepper implements TemplateTableOption {

    // A human-readable summary of this option.
    // This is displayed as the hint when registering a template.
    public String getDescription() {
        return "Example of a Feed Wizard plugin.";
    }

    // A human-readable title of this option.
    // This is displayed as the title of this option when registering a template.
    public String getDisplayName() {
        return "Example Feed Stepper";
    }

    // Template URL containing sections for viewing or editing a feed.
    public String getFeedDetailsTemplateUrl() {
        return "/example-ui-feed-stepper-plugin-1.0/feed-details.html";
    }

    // List of metadata properties that can be used in NiFi property expressions.
    public List<AnnotatedFieldProperty<MetadataField>> getMetadataProperties() {
        AnnotatedFieldProperty<MetadataField> property = new AnnotatedFieldProperty<>();
        property.setName("examplePluginProperty");
        property.setDescription("Example Plugin property");
        return Collections.singletonList(property);
    }

    // Template URL containing steps for creating or editing a feed.
    public String getStepperTemplateUrl() {
        return "/example-ui-feed-stepper-plugin-1.0/stepper.html";
    }

    // Number of additional steps for creating or editing a feed.
    public int getTotalSteps() {
        return 1;
    }

    // A unique identifier for this option.
    public String getType() {
        return "EXAMPLE_UI_FEED_STEPPER";
    }
}
```

Configuration
-------------

The plugin is discovered by providing a jar that contains a Spring bean in the kylo-ui app (/opt/kylo/kylo-ui/plugin/). The configuration class should also setup a URL mapping for its templates.

```java
public class ExampleFeedStepperConfig {

    // Create the plugin definition
    @Bean
    public ExampleFeedStepper exampleFeedStepper() {
        return new ExampleFeedStepper();
    }

    // Map web resources to classpath
    @Bean
    public WebMvcConfigurerAdapter myPluginWebMvcConfigurerAdapter() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/example-ui-feed-stepper-plugin-1.0/**").addResourceLocations("classpath:/example-ui-feed-stepper-plugin-1.0/");
            }
        };
    }
}
```

A `spring.factories` file in the `META-INF` folder will indicate which configuration classes should be loaded by Spring.

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.example.kylo.plugin.ExampleFeedStepperConfig
```

Stepper Template
----------------

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
