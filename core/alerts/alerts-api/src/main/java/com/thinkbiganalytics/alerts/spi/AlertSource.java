/**
 * 
 */
package com.thinkbiganalytics.alerts.spi;

import java.io.Serializable;
import java.util.Iterator;
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
    
    Alert getAlert(Alert.ID id);
    
    Iterator<? extends Alert> getAlerts();

    Iterator<? extends Alert> getAlertsSince(DateTime since);
    
    Iterator<? extends Alert> getAlertsSince(Alert.ID since);
}
