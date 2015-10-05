/**
 * 
 */
package com.thinkbiganalytics.alerts.spi;

import java.util.Iterator;
import java.util.Set;

import org.joda.time.DateTime;

import com.thinkbiganalytics.alerts.api.Alert;

/**
 *
 * @author Sean Felten
 */
public interface AlertSource {

    Set<AlertDescriptor> getAlertTypes();
    
    void addNodifier(AlertNotifyReceiver receiver);
    
    Iterator<? extends Alert> getAlerts();

    Iterator<? extends Alert> getAlerts(DateTime since);
    
    Iterator<? extends Alert> getAlerts(Alert.ID since);
}
