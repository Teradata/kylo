/**
 * 
 */
package com.thinkbiganalytics.alerts.spi;

import java.net.URI;

import com.thinkbiganalytics.alerts.api.Alert;

/**
 *
 * @author Sean Felten
 */
public interface AlertManager extends AlertSource {
    
    boolean addDescriptor(AlertDescriptor descriptor);

    <C> Alert create(URI type, Alert.Level level, String description, C content);
    
    <C> Alert changeState(Alert alert, Alert.State newState, C content);
    
    Alert remove(Alert.ID id);
}
