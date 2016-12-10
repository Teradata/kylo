/**
 * 
 */
package com.thinkbiganalytics.alerts.spi.kylo;

import java.io.Serializable;
import java.net.URI;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.joda.time.DateTime;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.ID;
import com.thinkbiganalytics.alerts.api.Alert.Level;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.spi.AlertDescriptor;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.AlertNotifyReceiver;

/**
 *
 * @author Sean Felten
 */
public class KyloAlertManager implements AlertManager {

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#resolve(java.io.Serializable)
     */
    @Override
    public ID resolve(Serializable value) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#getAlertDescriptors()
     */
    @Override
    public Set<AlertDescriptor> getAlertDescriptors() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#addReceiver(com.thinkbiganalytics.alerts.spi.AlertNotifyReceiver)
     */
    @Override
    public void addReceiver(AlertNotifyReceiver receiver) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#getAlert(com.thinkbiganalytics.alerts.api.Alert.ID)
     */
    @Override
    public Optional<Alert> getAlert(ID id) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#getAlerts()
     */
    @Override
    public Iterator<Alert> getAlerts() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#getAlerts(org.joda.time.DateTime)
     */
    @Override
    public Iterator<Alert> getAlerts(DateTime since) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertSource#getAlerts(com.thinkbiganalytics.alerts.api.Alert.ID)
     */
    @Override
    public Iterator<Alert> getAlerts(ID since) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertManager#addDescriptor(com.thinkbiganalytics.alerts.spi.AlertDescriptor)
     */
    @Override
    public boolean addDescriptor(AlertDescriptor descriptor) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertManager#create(java.net.URI, com.thinkbiganalytics.alerts.api.Alert.Level, java.lang.String, java.io.Serializable)
     */
    @Override
    public <C extends Serializable> Alert create(URI type, Level level, String description, C content) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertManager#getResponse(com.thinkbiganalytics.alerts.api.Alert)
     */
    @Override
    public AlertResponse getResponse(Alert alert) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.spi.AlertManager#remove(com.thinkbiganalytics.alerts.api.Alert.ID)
     */
    @Override
    public Alert remove(ID id) {
        // TODO Auto-generated method stub
        return null;
    }

}
