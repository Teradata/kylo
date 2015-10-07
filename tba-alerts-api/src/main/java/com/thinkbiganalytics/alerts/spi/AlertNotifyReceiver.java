/**
 * 
 */
package com.thinkbiganalytics.alerts.spi;

/**
 *
 * @author Sean Felten
 */
public interface AlertNotifyReceiver {

    void alertsAvailable(int count);
}
