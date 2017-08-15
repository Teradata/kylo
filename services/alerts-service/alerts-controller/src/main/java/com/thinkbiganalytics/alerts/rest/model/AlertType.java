package com.thinkbiganalytics.alerts.rest.model;

/**
 * Created by sr186054 on 8/15/17.
 */
public class AlertType {
    private String type;
    private String label;

    public AlertType(String type, String label) {
        this.type = type;
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
