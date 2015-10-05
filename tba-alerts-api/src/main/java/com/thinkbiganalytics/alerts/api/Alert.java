/**
 * 
 */
package com.thinkbiganalytics.alerts.api;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

import com.thinkbiganalytics.alerts.spi.AlertSource;

/**
 * This type defines the basic alert abstraction. Only the basic properties of an alert are defined
 * here.  This kind of object also acts as a container for the more type-specific details returned from
 * its content property.
 * @author Sean Felten
 */
public interface Alert {
    /** The opaque identifier of this event */
    interface ID extends Serializable { }
    
    /** 
     * The states that this alert may transition through, listed within the change events.  
     * For non-actionable alerts this state will never transition beyond created.
     */
    enum State { CREATED, UNHANDLED, IN_PROCESS, HANDLED, CLEARED }
    
    /** The severity level that alerts may have */
    enum Level { INFO, WARNING, MINOR, MAJOR, CRITICAL, FATAL }
    

    /**
     * A unique URI defining the type of alert this is.  URIs allow for a more heirarichical 
     * type structure.
     * @return the unique type URI
     */
    URI getType();
    
    /**
     * @return a description of this alert
     */
    String getDescription();
    
    /**
     * @return thie level of this alert
     */
    Level getLevel();
    
    /**
     * @return the alert source that produced this alert
     */
    AlertSource getSource();
    
    /**
     * AlertResponders will only be invoked when actionable alerts.  Alerts whose 
     * state may be changed by AlertResponder should have this method return true.
     * @return whether this alert is actionable
     */
    boolean isActionale();
    
    /**
     * Gets ordered list of state change events showing the state transitions this alert
     * has gone through.
     * @return the list of state changes
     */
    List<AlertChangeEvent> getEvents();
    
    /**
     * The payload containing type-specific data for this alert.  The kind of object
     * returned from this method is defined by the type of alert this is.
     * @return a particular content object based this alert type
     */
    <C> C getContent();
}
