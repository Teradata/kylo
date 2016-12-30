/**
 * 
 */
package com.thinkbiganalytics.alerts.spi;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertCriteria;

/**
 *
 * @author Sean Felten
 */
public interface AlertSource {
    
    AlertCriteria criteria();

    Alert.ID resolve(Serializable value);

    Set<AlertDescriptor> getAlertDescriptors();
    
    void addReceiver(AlertNotifyReceiver receiver);
    
    void removeReceiver(AlertNotifyReceiver receiver);
    
    Optional<Alert> getAlert(Alert.ID id);
    
    Iterator<Alert> getAlerts(AlertCriteria criteria);
}
