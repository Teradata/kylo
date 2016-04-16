/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.servicemonitor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * Created by sr186054 on 10/2/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultServiceAlert implements ServiceAlert {

    private String serviceName;
    private String componentName;
    private String label;
    private String message;
    private Date firstTimestamp;
    private Date latestTimestamp;
    private STATE state;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getFirstTimestamp() {
        return firstTimestamp;
    }

    public void setFirstTimestamp(Date firstTimestamp) {
        this.firstTimestamp = firstTimestamp;
    }

    public Date getLatestTimestamp() {
        return latestTimestamp;
    }

    public void setLatestTimestamp(Date latestTimestamp) {
        this.latestTimestamp = latestTimestamp;
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        this.state = state;
    }


}
