/**
 * 
 */
package com.thinkbiganalytics.alerts.rest.model;

import org.joda.time.DateTime;

import com.thinkbiganalytics.alerts.rest.model.Alert.State;

/**
 * Represents a change event of an alert.
 * 
 * @author Sean Felten
 */
public class AlertChangeEvent {

    private DateTime createdTime;
    private State state;
    private String description;

    public DateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
