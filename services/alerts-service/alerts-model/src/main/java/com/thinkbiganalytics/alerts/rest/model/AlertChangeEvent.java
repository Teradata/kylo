/**
 * 
 */
package com.thinkbiganalytics.alerts.rest.model;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.thinkbiganalytics.alerts.rest.model.Alert.State;

/**
 * Represents a change event of an alert.
 * 
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertChangeEvent {

    private DateTime createdTime;
    private State state;
    private String description;
    private String user;

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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
    
}
