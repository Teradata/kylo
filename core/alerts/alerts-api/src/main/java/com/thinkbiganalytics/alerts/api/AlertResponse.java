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
     */
    Alert inProgress();
    
    /**
     * Changes an alert status to in-progress.
     * @param content alert type-specific content associated with the state change
     */
    <C extends Serializable> Alert inProgress(C content);
    
    /**
     * Changes an alert status to handled.
     */
    Alert handle();
    
    /**
     * Changes an alert status to handled.
     * @param content alert type-specific content associated with the state change
     */
    <C extends Serializable> Alert handle(C content);
    
    /**
     * Changes an alert status to unhandled.
     */
    Alert unHandle();
    
    /**
     * Changes an alert status to unhandled.
     * @param content alert type-specific content associated with the state change
     */
    <C extends Serializable> Alert unhandle(C content);
    
    /**
     * clears (effectively removes) an alert.  No other responder will see this alert if cleared.
     */
    void clear();
}
