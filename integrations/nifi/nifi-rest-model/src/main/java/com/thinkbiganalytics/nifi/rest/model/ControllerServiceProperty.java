package com.thinkbiganalytics.nifi.rest.model;

/**
 * Created by sr186054 on 5/8/16.
 */
public class ControllerServiceProperty {


    private String processorId;
    private String processorName;
    private String processorGroupId;
    private String propertyName;
    private String propertyValue;
    private boolean valid;
    private String validationMessage;

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    public String getProcessorGroupId() {
        return processorGroupId;
    }

    public void setProcessorGroupId(String processorGroupId) {
        this.processorGroupId = processorGroupId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }
}