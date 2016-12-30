/**
 * 
 */
package com.thinkbiganalytics.alerts.rest.model;

import com.thinkbiganalytics.alerts.rest.model.Alert.State;

/**
 * Contains the details necessary to update the state of an alert.
 * 
 * @author Sean Felten
 */
public class AlertUpdateRequest {

    private Alert.State state;
    private String description;
    
    // TODO not supporting arbitrary content in REST model for now
//    private Serializable content;
//    private String contentType;

    
    
    public AlertUpdateRequest() {
    }

    public Alert.State getState() {
        return state;
    }

    public void setState(Alert.State state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    
}
