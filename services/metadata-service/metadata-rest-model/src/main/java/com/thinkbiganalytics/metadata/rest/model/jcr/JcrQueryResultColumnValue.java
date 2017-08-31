package com.thinkbiganalytics.metadata.rest.model.jcr;

/**
 * Created by sr186054 on 8/31/17.
 */
public class JcrQueryResultColumnValue {

    private String value;

    public JcrQueryResultColumnValue(String value) {
        this.value = value;
    }

    public JcrQueryResultColumnValue(){

    }
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
