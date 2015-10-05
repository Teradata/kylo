/**
 * 
 */
package com.thinkbiganalytics.alerts.api;

/**
 * Instances of this object provide a means for a responder to change the state of an actionable alert.
 * An instance is passed whenever an AlertResponder is notified of a alert change.  That response instance
 * only applicable within the context of that responder's alertChange() method.
 * @author Sean Felten
 */
public interface AlertResponse {

    /**
     * Changes an alert status to in-progress.
     * @param content alert type-specific content associated with the state change
     */
    <C> void inProgress(C content);
    
    /**
     * Changes an alert status to handled.
     * @param content alert type-specific content associated with the state change
     */
    <C> void handle(C content);
    
    /**
     * Changes an alert status to unhandled.
     * @param content alert type-specific content associated with the state change
     */
    <C> void unHandle(C content);
    
    /**
     * clear (effectively removes) an alert.  No other responder will see this alert if cleared.
     */
    void clear();
}
