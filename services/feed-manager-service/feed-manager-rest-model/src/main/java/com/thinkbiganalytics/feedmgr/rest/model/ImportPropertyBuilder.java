package com.thinkbiganalytics.feedmgr.rest.model;
/*-
 * #%L
 * thinkbig-feed-manager-rest-model
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
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public final class ImportPropertyBuilder {

    private ImportComponent importComponent;

    private String componentName;
    private String propertyKey;
    private String propertyValue;
    private String displayName;
    private String description;
    private String type;
    private String componentId;
    private boolean valid;

    private Map<String,String> additionalProperties;

    private ImportPropertyBuilder() {
    }

    public static ImportPropertyBuilder anImportProperty() {
        return new ImportPropertyBuilder();
    }

    public ImportPropertyBuilder withImportComponent(ImportComponent importComponent) {
        this.importComponent = importComponent;
        return this;
    }

    public ImportPropertyBuilder withPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
        return this;
    }

    public ImportPropertyBuilder withPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
        return this;
    }

    public ImportPropertyBuilder withComponentName(String componentName) {
        this.componentName = componentName;
        return this;
    }

    public ImportPropertyBuilder withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public ImportPropertyBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ImportPropertyBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public ImportPropertyBuilder withComponentId(String componentId) {
        this.componentId = componentId;
        return this;
    }

    public ImportPropertyBuilder asValid(boolean valid) {
        this.valid = valid;
        return this;
    }

    public ImportPropertyBuilder withAdditionalProperties(Map<String,String> additionalProperties){
        this.additionalProperties = additionalProperties;
        return this;
    }

    public ImportPropertyBuilder putAdditionalProperty(String key, String value){
        if(this.additionalProperties == null){
            this.additionalProperties = new HashMap<>();
        }
        this.additionalProperties.put(key,value);
        return this;
    }

    public ImportProperty build() {
        ImportProperty importProperty = new ImportProperty();
        importProperty.setImportComponent(importComponent);

        importProperty.setComponentName(componentName);
        importProperty.setProcessorName(componentName);
        importProperty.setProcessorId(componentId);
        importProperty.setComponentId(componentId);

        importProperty.setProcessorType(type);
        importProperty.setType(type);

        importProperty.setAdditionalProperties(additionalProperties);

        if (StringUtils.isBlank(displayName) && StringUtils.isNotBlank(propertyKey)) {
            displayName = propertyKey;
        }
        if (StringUtils.isBlank(propertyKey) && StringUtils.isNotBlank(displayName)) {
            propertyKey = displayName;
        }
        importProperty.setPropertyKey(propertyKey);
        importProperty.setDisplayName(displayName);


        importProperty.setPropertyValue(propertyValue);
        importProperty.setDescription(description);
        importProperty.setValid(valid);
        return importProperty;
    }
}
