package com.example.kylo.plugin;

/*-
 * #%L
 * example-ui-feed-stepper-plugin
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.annotations.AnnotatedFieldProperty;
import com.thinkbiganalytics.metadata.MetadataField;
import com.thinkbiganalytics.ui.api.template.TemplateTableOption;

import java.util.Collections;
import java.util.List;

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
