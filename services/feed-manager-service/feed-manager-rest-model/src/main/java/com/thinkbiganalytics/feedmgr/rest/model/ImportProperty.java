package com.thinkbiganalytics.feedmgr.rest.model;

/**
 * Created by sr186054 on 3/5/17.
 */
public class ImportProperty {


    private String processorName;
    private String processorId;
    private String processorType;
    private String propertyKey;
    private String propertyValue;

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
}
