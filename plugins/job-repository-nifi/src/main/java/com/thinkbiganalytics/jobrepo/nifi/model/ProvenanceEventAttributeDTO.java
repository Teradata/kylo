package com.thinkbiganalytics.jobrepo.nifi.model;

import java.io.Serializable;

/**
 * Created by sr186054 on 5/10/16.
 */
public class ProvenanceEventAttributeDTO implements Serializable{

    private String name;
    private String value;
    private String previousValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(String previousValue) {
        this.previousValue = previousValue;
    }
}
