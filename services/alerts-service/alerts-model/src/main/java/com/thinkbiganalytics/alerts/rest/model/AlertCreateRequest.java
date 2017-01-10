/**
 * 
 */
package com.thinkbiganalytics.alerts.rest.model;

import java.io.Serializable;
import java.net.URI;

import com.thinkbiganalytics.alerts.rest.model.Alert.Level;

/**
 * Contains the details necessary to create a new alert.
 * 
 * @author Sean Felten
 */
public class AlertCreateRequest {
    
    private URI type;
    private String description;
    private Level level; 
    
    // TODO not supporting arbitrary content in REST model for now
//    private Serializable content;
//    private String contentType;
    
    public AlertCreateRequest() {
    }
    
    public AlertCreateRequest(URI type, String description, Level level) {
        this(type, description, level, null);
    }

    public AlertCreateRequest(URI type, String description, Level level, Serializable content) {
        super();
        this.type = type;
        this.description = description;
        this.level = level;
    }

    public URI getType() {
        return type;
    }

    public void setType(URI type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

}
