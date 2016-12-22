/**
 * 
 */
package com.thinkbiganalytics.alerts.spi;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.joda.time.DateTime;

import com.thinkbiganalytics.alerts.api.Alert;

/**
 *
 * @author Sean Felten
 */
public interface AlertSource {

    Alert.ID resolve(Serializable value);

    Set<AlertDescriptor> getAlertDescriptors();
    
    void addReceiver(AlertNotifyReceiver receiver);
    
    void removeReceiver(AlertNotifyReceiver receiver);
    
    Optional<Alert> getAlert(Alert.ID id);
    
    Iterator<Alert> getAlerts();

    Iterator<Alert> getAlerts(DateTime since);
    
    Iterator<Alert> getAlerts(Alert.ID since);
}
