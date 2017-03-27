package com.thinkbiganalytics.feedmgr.rest.model;

/**
 * Created by sr186054 on 3/5/17.
 */
public class ImportFeedProperty {


    private String processorName;
    private String processorId;
    private String propertyKey;
    private String propertyValue;

    public ImportFeedProperty(){

    }
    public ImportFeedProperty(String processorName, String processorId, String propertyKey, String propertyValue) {
        this.processorName = processorName;
        this.processorId = processorId;
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
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
}
