/**
 * 
 */
package com.thinkbiganalytics.alerts.api;

import java.util.Iterator;

import org.joda.time.DateTime;

/**
 * This provider is the primary facade over one or more alert sources.  It allows
 * for synchronously retrieving and responding to alerts, as well as registering listeners/
 * responders to react to new event entering the system.
 * @author Sean Felten
 */
public interface AlertProvider {
    
    // TODO: add criteria-based search
    
    /**
     * Registers a listener that will be called whenever the state of an alert
     * changes, such as when one is created or responded to by responder that 
     * responded to an alert and caused its state to change.
     * @param listener the listener being added
     */
    void addListener(AlertListener listener);
    
    /**
     * Registers a responder that will be invoked whenever an alert has transitioned into a non-terminal
     * state (ie. CREATED, UNHANDLED, IN_PROCESS).  
     * @param responder
     */
    void addResponder(AlertResponder responder);
    
    Iterator<? extends Alert> getAlerts(DateTime since);
    
    Iterator<? extends Alert> getAlerts(Alert.ID since);

    void respondTo(Alert.ID id, AlertResponder responder);
}
