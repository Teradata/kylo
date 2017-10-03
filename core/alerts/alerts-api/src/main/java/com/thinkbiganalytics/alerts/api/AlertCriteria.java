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

import com.thinkbiganalytics.alerts.api.Alert.Level;
import com.thinkbiganalytics.alerts.api.Alert.State;

import org.joda.time.DateTime;

import java.net.URI;

/**
 *
 */
public interface AlertCriteria {


    /**
     * Sets the maximum size of the list alerts retrieved.  The default, if no
     * limit is given, is AlertSource dependent.
     *
     * @param size the maximum number of alerts to return
     * @return the updated criteria
     */
    AlertCriteria limit(int size);

    /**
     * @param type   a type upon which to filter
     * @param others TODO
     * @return the updated criteria
     */
    AlertCriteria type(URI type, URI... others);

    /**
     * @param type   a type upon which to filter
     * @param others TODO
     * @return the updated criteria
     */
    AlertCriteria subtype(String subtype, String... others);

    /**
     * @param state  a state upon which to filter
     * @param others TODO
     * @return the updated criteria
     */
    AlertCriteria state(State state, State... others);

    /**
     * @param level  a level upon which to filter
     * @param others TODO
     * @return the updated criteria
     */
    AlertCriteria level(Level level, Level... others);

    /**
     * Filters alerts to retrieve only those newer than the specified time.
     *
     * @param time the maximum time
     * @return the updated criteria
     */
    AlertCriteria after(DateTime time);

    /**
     * Filters alerts to retrieve only those older than the specified time.
     *
     * @param time the minimum time
     * @return the updated criteria
     */
    AlertCriteria modifiedBefore(DateTime time);

    /**
     * Filters alerts to retrieve only those newer than the specified time.
     *
     * @param time the maximum time
     * @return the updated criteria
     */
    AlertCriteria modifiedAfter(DateTime time);

    /**
     * Filters alerts to retrieve only those older than the specified time.
     *
     * @param time the minimum time
     * @return the updated criteria
     */
    AlertCriteria before(DateTime time);

    /**
     * @param flag set to true if cleared alerts should be retrieved (default false)
     * @return the updated criteria
     */
    AlertCriteria includedCleared(boolean flag);

    /**
     * filter on all fields in an or condition
     * @param orFilter the filter string
     * @return the updated criteria
     */
    AlertCriteria orFilter(String orFilter);

    AlertCriteria asServiceAccount(boolean serviceAccount);

    boolean isAsServiceAccount();

    /**
     * if set to true the Manager querying alerts will only query if the manager has changed/updated/created alerts
     * false will always query
     * @return
     */
    AlertCriteria onlyIfChangesDetected(boolean onlyIfChangesDetected);

    boolean isOnlyIfChangesDetected();

}
