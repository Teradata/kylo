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

import com.thinkbiganalytics.alerts.api.Alert.State;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.security.Principal;

/**
 * Each instance of this type represents a state transition of an alert.  Initially, all alerts
 * will start with one change event: either UNHANDLED or CREATED.  Actionable alerts will have the
 * former event, and non-actionable alerts with the latter.
 */
public interface AlertChangeEvent {

    /**
     * @return the time when the alert transitioned to this state
     */
    DateTime getChangeTime();

    /**
     * @return the principal of the user that created the alert
     */
    Principal getUser();

    /**
     * @return the new state
     */
    State getState();

    /**
     * @return a description of the change (may be null)
     */
    String getDescription();

    /**
     * Any state change may have a piece of information associated with it.  The type of object
     * returned by this method is specific to the type of alert that was changed.
     *
     * @return an alert-specific piece of data that may be associated with this state
     */
    <C extends Serializable> C getContent();

}
