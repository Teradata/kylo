/**
 *
 */
package com.thinkbiganalytics.alerts.spi;

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

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertCriteria;
import com.thinkbiganalytics.alerts.api.AlertSummary;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 *
 */
public interface AlertSource {

    AlertCriteria criteria();

    Alert.ID resolve(Serializable value);

    Set<AlertDescriptor> getAlertDescriptors();

    void addReceiver(AlertNotifyReceiver receiver);

    void removeReceiver(AlertNotifyReceiver receiver);

    /**
     * Gets the alert as the currently logged in User
     * @param id the id of the alert
     * @return the alert
     */
    Optional<Alert> getAlert(Alert.ID id);

    /**
     * Gets an alert as a Service Account (privileged user)
     * @param id the id of the alert
     * @return the Alert
     */
    Optional<Alert> getAlertAsServiceAccount(Alert.ID id);

    Iterator<Alert> getAlerts(AlertCriteria criteria);

    Iterator<AlertSummary> getAlertsSummary(AlertCriteria criteria);

    /**
     *
     * @return the id for this source
     */
    ID getId();

    /**
     * The opaque identifier of this source
     */
    interface ID extends Serializable {

    }
}
