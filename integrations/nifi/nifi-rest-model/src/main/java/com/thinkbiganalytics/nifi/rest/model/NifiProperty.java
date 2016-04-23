/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.nifi.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.PropertyDescriptorDTO;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by sr186054 on 1/11/16.
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class NifiProperty {

    private String processGroupName;
    private String processorName;
    private String processorId;
    private String processGroupId;
    private String processorType;
    private String key;
    private String value;
    private String templateValue;
    private boolean userEditable;
    private List<String> expressionProperties;
    private PropertyDescriptorDTO propertyDescriptor;
    private ProcessorDTO processor;

    private String renderType; // checkbox, input, editor-hive, editor-sql, editor-pig, etc

    private boolean selected;
    private boolean inputProperty;


    private NifiProperty templateProperty; // a copy of the Template Property so it can be referenced back to when displaying data between the Feed and the template used;

    public boolean isInputProperty() {
        return inputProperty;
    }

    public void setInputProperty(boolean inputProperty) {
        this.inputProperty = inputProperty;
    }

    public NifiProperty() {


    }

    public NifiProperty(NifiProperty property) {
        this.processGroupName = property.getProcessGroupName();
        this.processorName = property.getProcessorName();
        this.processorId = property.getProcessorId();
        this.processGroupId = property.getProcessGroupId();
        this.key = property.getKey();
        this.value = property.getValue();
        this.templateValue = property.getTemplateValue();
        this.userEditable = property.isUserEditable();
        this.expressionProperties = property.getExpressionProperties();
        this.propertyDescriptor = property.getPropertyDescriptor();
        this.processor = property.getProcessor();
        this.renderType = property.getRenderType();
        this.selected = property.isSelected();
        this.inputProperty = property.isInputProperty();
        this.processorName = property.getProcessorType();
    }

    public NifiProperty(String processGroupId,String processorId, String key, String value) {
        this.processGroupId = processGroupId;
        this.processorId = processorId;
        this.key = key;
        this.value = value;
    }

    public NifiProperty(String processGroupId,String processorId, String key, String value, PropertyDescriptorDTO propertyDescriptor) {
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

    public PropertyDescriptorDTO getPropertyDescriptor() {
        return propertyDescriptor;
    }

    public void setPropertyDescriptor(PropertyDescriptorDTO propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    public ProcessorDTO getProcessor() {
        return processor;
    }

    public void setProcessor(ProcessorDTO processor) {
        this.processor = processor;
    }

    public String getIdKey() {
        return this.getProcessGroupId()+"-"+this.getProcessorId()+"-"+this.getKey();
    }

    public String getNameKey() {
        return this.getProcessGroupName()+"-"+this.getProcessorName()+"-"+this.getKey();
    }

    public List<String> getExpressionProperties() {
        return expressionProperties;
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

    public boolean matchesIdKey(NifiProperty property){
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
}
