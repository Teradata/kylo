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

/**
 * Defines a listener for changes to the state of an alert.  Note that, depending on the
 * implementation of the alert provider, all listeners may be invoked in parallel, and
 * that the actual state of an alert may have changed again before this notification
 * has been delivered.
 */
public interface AlertListener {

    /**
     * Invoked when the state of an alert has changed.  This will be invoked for an alert
     * at least once when it is created.  Subsequent invocations may occur for an alert
     * if it is actionable.  The state transition that triggered this call will be the
     * first event listed for the given alert.
     *
     * @param alert the alert that changed
     */
    void alertChange(Alert alert);
}
