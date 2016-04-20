/**
 * 
 */
package com.thinkbiganalytics.alerts.spi;

import java.net.URI;

import com.thinkbiganalytics.alerts.api.Alert;

/**
 * A kind of AlertSource that provides alert creation and change management functions.
 * 
 * @author Sean Felten
 */
public interface AlertManager extends AlertSource {
    
    /**
     * Adds a new descriptor that this manager will provide.
     * @param descriptor the descriptor to add
     * @return true if the descriptor was not previously present
     */
    boolean addDescriptor(AlertDescriptor descriptor);

    /**
     * Creates a new alert and adds 
     * 
     * @param type the type
     * @param level the level
     * @param description a description of the alert
     * @param content optional content, the type of which is specific to the kind of alert
     * @return
     */
    <C> Alert create(URI type, Alert.Level level, String description, C content);
    
    /**
     * Creates a new alert from the given alert with with a new state and associated content.
     * 
     * @param alert the source alert
     * @param newState the new state for the resulting alert
     * @param content optional content, the type of which is specific to the kind of alert and state
     * @return the new alert
     */
    <C> Alert changeState(Alert alert, Alert.State newState, C content);
    
    /**
     * Removes an alert from the manager.
     * 
     * @param id the ID of the alert to remove
     * @return the alert that was removed
     */
    Alert remove(Alert.ID id);
}
