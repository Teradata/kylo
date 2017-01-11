/**
 * 
 */
package com.thinkbiganalytics.alerts.api;

import java.io.Serializable;
import java.net.URI;
import java.security.Principal;
import java.util.List;

import org.joda.time.DateTime;

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
    enum State { CREATED, UNHANDLED, IN_PROGRESS, HANDLED }
    
    /** The severity level that alerts may have */
    enum Level { INFO, WARNING, MINOR, MAJOR, CRITICAL, FATAL }
    
    
    /**
     * @return the ID of the alert
     */
    ID getId();
    
    /**
     * A unique URI defining the type of alert this is.  URIs allow for a more heirarichical 
     * name space defining the type.
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
     * Gets the time when this alert was created.  Note that it is usually the same
     * time as the change time of the oldest event in the change event list.
     * @return the time this alert was created.
     */
    DateTime getCreatedTime();

    /**
     * @return the alert source that produced this alert
     */
    AlertSource getSource();
    
    /**
     * AlertResponders will only be invoked for actionable alerts.  Alerts whose 
     * state may be changed by AlertResponders should have this method return true.
     * @return whether this alert is actionable
     */
    boolean isActionable();
    
    /**
     * Retrieves the current state of this alert.  If this alert supports events then this is
     * the same state of the latest change event.
     * @return the current state of this alert
     */
    State getState();
    
    /**
     * Indicates whether this alert is considered cleared or not.  Normally cleared alerts do not 
     * appear in regular search results.
     * @return 
     */
    boolean isCleared();
    
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
    <C extends Serializable> C getContent();
}
