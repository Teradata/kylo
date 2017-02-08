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
 * Defines a kind of listener that may respond to and the change of the state
 * of an alert.  All responders that have been registered with the same alert provider are
 * guaranteed to see the latest state of the alert by the time this responder is invoked.
 * In other words, all responders will see any subsequent state changes that preceding responders
 * may have triggered before this responder was triggered.
 */
public interface AlertResponder {

    /**
     * Called whenever the state of an alert has changed.  Any state changes that this responder wishes
     * to make to the alert should be made using the given response object.
     *
     * @param alert    the alert that changed
     * @param response a response object to make any additional state changes
     */
    void alertChange(Alert alert, AlertResponse response);
}
