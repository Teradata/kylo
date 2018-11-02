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

import java.util.Map;

public class ImportProperty {

    private ImportComponent importComponent;


    private String processorName;
    private String processorId;
    private String processorType;

    private String componentName;
    private String componentId;
    private String propertyKey;
    private String propertyValue;

    private String displayName;
    private String description;
    private String type;
    private boolean valid;

    public Map<String,String> additionalProperties;


    public ImportProperty() {

    }


    public ImportProperty(String processorName, String processorId, String propertyKey, String propertyValue, String processorType) {
        this.processorName = processorName;
        this.processorId = processorId;
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
        this.processorType = processorType;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getProcessorType() {
        return processorType;
    }

    public void setProcessorType(String processorType) {
        this.processorType = processorType;
    }

    public String getProcessorNameTypeKey() {
        return this.getProcessorName() + "-" + this.getProcessorType() + "-" + this.getPropertyKey();
    }

    public String getDisplayName() {
        return displayName == null ? getPropertyKey() : displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ImportComponent getImportComponent() {
        return importComponent;
    }

    public void setImportComponent(ImportComponent importComponent) {
        this.importComponent = importComponent;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public String getAdditionalPropertyValue(String additionalPropertyKey){
        return this.additionalProperties != null ? this.additionalProperties.get(additionalPropertyKey) : null;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ImportProperty{");
        sb.append("importComponent=").append(importComponent);
        sb.append(", componentId='").append(componentId).append('\'');
        sb.append(", propertyKey='").append(propertyKey).append('\'');
        sb.append(", propertyValue='").append(propertyValue).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
