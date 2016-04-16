package com.thinkbiganalytics.jobrepo.query.model;

/**
 * Created by sr186054 on 9/14/15.
 */
public class JobParameterTypeImpl implements JobParameterType {

private String name;
    private String value;
    private String type;

    public JobParameterTypeImpl(String name, String value, String type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
}
