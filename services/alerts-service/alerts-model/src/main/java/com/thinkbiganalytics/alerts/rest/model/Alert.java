/**
 * 
 */
package com.thinkbiganalytics.alerts.rest.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents an alert.
 * 
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Alert {

    public enum Level {
        INFO, WARNING, MINOR, MAJOR, CRITICAL, FATAL
    }

    public enum State {
        CREATED, UNHANDLED, IN_PROGRESS, HANDLED
    }


    private String id;
    private URI type;
    private Level level;
    private State state;
    private DateTime createdTime;
    private String description;
    private boolean actionable;
    private boolean cleared;
    private List<AlertChangeEvent> events = new ArrayList<>();
    
    public Alert() {}



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public URI getType() {
        return type;
    }

    public void setType(URI type) {
        this.type = type;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public DateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isCleared() {
        return cleared;
    }

    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }

    public boolean isActionable() {
        return actionable;
    }

    public void setActionable(boolean actionable) {
        this.actionable = actionable;
    }
    
    public List<AlertChangeEvent> getEvents() {
        return events;
    }
}
