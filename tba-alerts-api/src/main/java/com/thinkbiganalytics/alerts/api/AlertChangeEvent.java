/**
 * 
 */
package com.thinkbiganalytics.alerts.api;

import org.joda.time.DateTime;

import com.thinkbiganalytics.alerts.api.Alert.State;

/**
 *
 * @author Sean Felten
 */
public interface AlertChangeEvent {

    Alert.ID getAlertId();
    
    DateTime getChangeTime();
    
    State getNewState();

    <C> C getContent();

}
