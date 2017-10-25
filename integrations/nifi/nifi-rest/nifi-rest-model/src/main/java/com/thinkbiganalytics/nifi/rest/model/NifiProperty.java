package com.thinkbiganalytics.nifi.rest.model;

/*-
 * #%L
 * thinkbig-nifi-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a NiFi property for a processor or controller service along with render options indicating how it should be rendered in the user interface
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class NifiProperty {

    /**
     * Nifi property options from the Nifi dto
     **/
    private String processGroupName;
    private String processorName;
    private String processorId;
    private String processGroupId;
    private String processorType;
    private String key;
    private String value;
    private List<String> expressionProperties;
    private NiFiPropertyDescriptor propertyDescriptor;


    /**
     * copy of the value stored in the template, vs the on value stored here in the property to compare any changes
     **/
    private String templateValue;

    /**
     * flag indicating the admin chose to allow the user to configure this property when creating a feed
     **/
    private boolean userEditable;


    /**
     * how should the property be rendered
     **/
    private String renderType; // checkbox, input, editor-hive, editor-sql, editor-pig, etc

    /**
     * Additional options for rendering  such as selectable values
     **/
    private Map<String, String> renderOptions;

    /**
     * flag indicating the property is selected for modification in the kylo ui
     **/
    private boolean selected;
    /**
     * flag indicating this property is part of a processor who is an 'input' or processor without any incoming connections
     **/
    private boolean inputProperty;

    /**
     * flag indicating the value of the template has ${config.} properties that need to be substituted
     */
    private boolean containsConfigurationVariables;

    /**
     * flag to indicate the property is sensitive.  Kylo will encrypt the sensitive properties before saving in the metadata.
     */
    private boolean sensitive;

    /**
     * Flag to indicate the property is required.
     */
    private boolean required;

    /**
     * a copy of the Template Property so it can be referenced back to when displaying data between the Feed and the template used
     **/
    private NifiProperty templateProperty;

    public NifiProperty() {

    }

    public NifiProperty(NifiProperty property) {
        this.processGroupName = property.getProcessGroupName();
        this.processorName = property.getProcessorName();
        this.processorId = property.getProcessorId();
        this.processGroupId = property.getProcessGroupId();
        this.processorType = property.getProcessorType();
        this.key = property.getKey();
        this.value = property.getValue();
        this.templateValue = property.getTemplateValue();
        this.userEditable = property.isUserEditable();
        this.expressionProperties = property.getExpressionProperties();
        this.propertyDescriptor = property.getPropertyDescriptor();
        this.renderType = property.getRenderType();
        this.selected = property.isSelected();
        this.inputProperty = property.isInputProperty();
        this.containsConfigurationVariables = property.isContainsConfigurationVariables();
        this.sensitive = property.isSensitive();
        this.required = property.isRequired();
    }

    public NifiProperty(String processGroupId, String processorId, String key, String value) {
        this.processGroupId = processGroupId;
        this.processorId = processorId;
        this.key = key;
        this.value = value;
    }

    public NifiProperty(String processGroupId, String processorId, String key, String value, NiFiPropertyDescriptor propertyDescriptor) {
        this.processGroupId = processGroupId;
        this.processorId = processorId;
        this.key = key;
        this.value = value;
        this.propertyDescriptor = propertyDescriptor;
    }

    public String getProcessGroupName() {
        return processGroupName;
    }

    public void setProcessGroupName(String processGroupName) {
        this.processGroupName = processGroupName;
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

    public String getProcessGroupId() {
        return processGroupId;
    }

    public void setProcessGroupId(String processGroupId) {
        this.processGroupId = processGroupId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public NiFiPropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }

    public void setPropertyDescriptor(NiFiPropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    /**
     * return a key string joining this properties processGroupId with the processorId and the property key
     *
     * @return a key string joining this properties processGroupId with the processorId and the property key
     */
    public String getIdKey() {
        return this.getProcessGroupId() + "-" + this.getProcessorId() + "-" + this.getKey();
    }

    public String getNameKey() {
        return this.getProcessGroupName() + "-" + this.getProcessorName() + "-" + this.getKey();
    }

    public List<String> getExpressionProperties() {
        return expressionProperties;
    }

    public String getProcessorNameTypeKey() {
        return this.getProcessorName() + "-" + this.getProcessorType() + "-" + this.getKey();
    }


    public void setExpressionProperties(List<String> expressionProperties) {
        this.expressionProperties = expressionProperties;
    }

    public String getTemplateValue() {
        return templateValue;
    }

    public void setTemplateValue(String templateValue) {
        this.templateValue = templateValue;
    }

    public boolean isUserEditable() {
        return userEditable;
    }

    public void setUserEditable(boolean userEditable) {
        this.userEditable = userEditable;
    }

    public boolean matchesIdKey(NifiProperty property) {
        return this.getIdKey().equalsIgnoreCase(property.getIdKey());
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getRenderType() {
        return renderType;
    }

    public void setRenderType(String renderType) {
        this.renderType = renderType;
    }

    public NifiProperty getTemplateProperty() {
        return templateProperty;
    }

    public void setTemplateProperty(NifiProperty templateProperty) {
        this.templateProperty = templateProperty;
    }


    public String getProcessorType() {
        return processorType;
    }

    public void setProcessorType(String processorType) {
        this.processorType = processorType;
    }

    public Map<String, String> getRenderOptions() {
        return renderOptions;
    }

    public void setRenderOptions(Map<String, String> renderOptions) {
        this.renderOptions = renderOptions;
    }

    public boolean isInputProperty() {
        return inputProperty;
    }

    public void setInputProperty(boolean inputProperty) {
        this.inputProperty = inputProperty;
    }

    public boolean isContainsConfigurationVariables() {
        return containsConfigurationVariables;
    }

    public void setContainsConfigurationVariables(boolean containsConfigurationVariables) {
        this.containsConfigurationVariables = containsConfigurationVariables;
    }

    @JsonIgnore
    public void resetToTemplateValue(){
        setValue(getTemplateValue());
    }

    public boolean isSensitive() {
        return sensitive || (getPropertyDescriptor() != null && getPropertyDescriptor().isSensitive() != null && getPropertyDescriptor().isSensitive());
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public boolean isRequired() {
        return required  || (getPropertyDescriptor() != null && getPropertyDescriptor().isRequired() != null && getPropertyDescriptor().isRequired());
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
