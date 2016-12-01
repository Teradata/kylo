package com.thinkbiganalytics.metadata.api;

/**
 * Created by sr186054 on 11/29/16.
 */
public class SearchCriteria {
    private String key;
    private String operation;
    private Object value;


    public SearchCriteria(){

    }
    public SearchCriteria(String key, String operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
