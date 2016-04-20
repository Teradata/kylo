/**
 * 
 */
package com.thinkbiganalytics.alerts.api;

/**
 * Defines a listener for changes to the state of an alert.  Note that, depending on the 
 * implementation of the alert provider, all listeners may be invoked in parallel, and
 * that the actual state of an alert may have changed again before this notification
 * has been delivered.
 * 
 * @author Sean Felten
 */
public interface AlertListener {

    /**
     * Invoked when the state of an alert has changed.  This will be invoked for an alert
     * at least once when it is created.  Subsequent invocations may occur for an alert
     * if it is actionable.  The state transition that triggered this call will be the
     * first event listed for the given alert.
     * @param alert the alert that changed
     */
    void alertChange(Alert alert);
}
