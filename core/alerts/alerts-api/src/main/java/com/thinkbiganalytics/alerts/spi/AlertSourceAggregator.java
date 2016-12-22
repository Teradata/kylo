/**
 * 
 */
package com.thinkbiganalytics.alerts.spi;

/**
 * Service provider interface that aggregates alert sources/managers by allowing each
 * to be registered by an implementor of this interface.
 * 
 * @author Sean Felten
 */
public interface AlertSourceAggregator {

    /**
     * @param src the AlertSource to add
     * @return true if this source had not previously been added
     */
    boolean addAlertSource(AlertSource src);
    
    /**
     * @param src the AlertSource to remove
     * @return true if this source existed and has been removed
     */
    boolean removeAlertSource(AlertSource src);
    
    /**
     * @param mgr the AlertManager to add
     * @return true if this manager had not previously been added
     */
    boolean addAlertManager(AlertManager mgr);
    
    /**
     * @param mgr the AlertManager to remove
     * @return true if this manager existed and has been removed
     */
    boolean removeAlertManager(AlertManager mgr);
}
