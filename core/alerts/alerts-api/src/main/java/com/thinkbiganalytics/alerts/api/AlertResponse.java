/**
 * 
 */
package com.thinkbiganalytics.alerts.api;

import java.io.Serializable;

/**
 * Instances of this object provide a means for a responder to change the state of an actionable alert.
 * An instance is passed whenever an AlertResponder is notified of a alert change.  That response instance
 * is only applicable within the context of that responder's alertChange() method.
 * @author Sean Felten
 */
public interface AlertResponse {

    /**
     * Changes an alert status to in-progress.
     * @param description a description of the change (may be null)
     */
    Alert inProgress(String description);
    
    /**
     * Changes an alert status to in-progress.
     * @param description a description of the change (may be null)
     * @param content alert type-specific content associated with the state change
     */
    <C extends Serializable> Alert inProgress(String description, C content);
    
    /**
     * Changes an alert status to handled.
     * @param description a description of the change (may be null)
     */
    Alert handle(String description);
    
    /**
     * Changes an alert status to handled.
     * @param description a description of the change (may be null)
     * @param content alert type-specific content associated with the state change
     */
    <C extends Serializable> Alert handle(String description, C content);
    
    /**
     * Changes an alert status to unhandled.
     * @param description a description of the change (may be null)
     */
    Alert unHandle(String description);
    
    /**
     * Changes an alert status to unhandled.
     * @param description a description of the change (may be null)
     * @param content alert type-specific content associated with the state change
     */
    <C extends Serializable> Alert unhandle(String description, C content);
    
    /**
     * clears (effectively removes) an alert.  No other responder will see this alert if cleared.
     */
    void clear();
}
