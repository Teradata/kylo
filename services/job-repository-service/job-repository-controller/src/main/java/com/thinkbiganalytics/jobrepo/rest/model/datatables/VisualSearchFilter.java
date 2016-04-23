package com.thinkbiganalytics.jobrepo.rest.model.datatables;

/**
 * Created by sr186054 on 8/17/15.
 */
public class VisualSearchFilter {
    private String key;
    private String value;
    private String operator;
    private String dataType = "string";

    public VisualSearchFilter() {

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

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
