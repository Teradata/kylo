/**
 * 
 */
package com.thinkbiganalytics.alerts.api;

/**
 * Defines a kind of listener that may respond to and the change of the state 
 * of an alert.  All responders that have been registered with the same alert provider are
 * guaranteed to see the latest state of the alert by the time this responder is invoked.
 * In other words, all responders will see any subsequent state changes that preceding responders
 * may have triggered before this responder was triggered.
 * @author Sean Felten
 */
public interface AlertResponder {

    /**
     * Called whenever the state of an alert has changed.  Any state changes that this responder wishes
     * to make to the alert should be made using the given response object.
     * @param alert the alert that changed
     * @param response a response object to make any additional state changes
     */
    void alertChange(Alert alert, AlertResponse response);
}
