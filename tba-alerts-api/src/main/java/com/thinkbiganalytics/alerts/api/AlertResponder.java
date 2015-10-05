/**
 * 
 */
package com.thinkbiganalytics.alerts.api;

/**
 *
 * @author Sean Felten
 */
public interface AlertResponder {

    void alerted(Alert alert, AlertResponse response);
}
