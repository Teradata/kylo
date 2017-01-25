/**
 * 
 */
package com.thinkbiganalytics.alerts.api;

/*-
 * #%L
 * thinkbig-alerts-api
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.util.Iterator;
import java.util.Optional;

import org.joda.time.DateTime;

/**
 * This provider is the primary facade over one or more alert sources.  It allows
 * for synchronously retrieving and responding to alerts, as well as registering listeners/
 * responders to react to new alerts that enter the system.
 * 
 * @author Sean Felten
 */
public interface AlertProvider {
    
    // TODO: add criteria-based listener/responder alert filtering
    
    /**
     * @return a criteria used to filter alerts
     */
    AlertCriteria criteria();
    
    /**
     * Resolves and reconstitutes one of the acceptable serializable ID formats (such as its toString() form) into 
     * a valid alert ID.
     * @param value one of the serializable formats
     * @return an alert ID
     */
    Alert.ID resolve(Serializable value);
    
    /**
     * Registers a listener that will be called whenever the state of an alert
     * changes, such as when one is created or responded to by responder that 
     * responded to an alert and caused its state to change.
     * @param listener the listener being added
     */
    void addListener(AlertListener listener);
    
    /**
     * Registers a responder that will be invoked whenever an respondable alert has transitioned new
     * state other than CLEARED. 
     * @param responder
     */
    void addResponder(AlertResponder responder);
    
    /**
     * Retrieves a specific alert
     * @param id the alert's ID
     * @return the alert, or null if no alert exists with that ID
     */
    Optional<Alert> getAlert(Alert.ID id);
    
    /**
     * Retrieves alerts matching the given criteria.  Specifying null retrieve all known alerts.
     * @param criteria
     * @return
     */
    Iterator<? extends Alert> getAlerts(AlertCriteria criteria);

    /**
     * Retrieves all alerts that may have been created after the given time.  
     * @param time the time from which newer alerts should be returned
     * @return an iterator on all alerts created after the specified time
     */
    Iterator<? extends Alert> getAlertsAfter(DateTime time);
    
    /**
     * Retrieves all alerts that may have been created after the given time.  
     * @param time the time from which newer alerts should be returned
     * @return an iterator on all alerts created after the specified time
     */
    Iterator<? extends Alert> getAlertsBefore(DateTime time);

    /**
     * Allows a synchronous update of a particular alert using the supplied responder.  This method will block
     * until the call to the responder has returned.
     * @param id the ID of the alert to respond to
     * @param responder the responder used as a call-back to respond to the alert
     */
    void respondTo(Alert.ID id, AlertResponder responder);
}
