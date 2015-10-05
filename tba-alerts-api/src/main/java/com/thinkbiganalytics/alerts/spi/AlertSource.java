/**
 * 
 */
package com.thinkbiganalytics.alerts.spi;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import org.joda.time.DateTime;

import com.thinkbiganalytics.alerts.api.Alert;

/**
 *
 * @author Sean Felten
 */
public interface AlertSource {

    Map<URI, AlertDescriptor> getAlertTypes();
    
    void addNodifier(AlertNotifyReceiver receiver);
    
    Iterator<? extends Alert> getAlerts();

    Iterator<? extends Alert> getAlerts(DateTime since);
    
    Iterator<? extends Alert> getAlerts(Alert.ID since);
}
