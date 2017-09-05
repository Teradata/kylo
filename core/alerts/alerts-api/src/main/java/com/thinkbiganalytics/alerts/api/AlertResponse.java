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

/**
 * Instances of this object provide a means for a responder to change the state of an actionable alert.
 * An instance is passed whenever an AlertResponder is notified of a alert change.  That response instance
 * is only applicable within the context of that responder's alertChange() method.
 */
public interface AlertResponse {

    /**
     * Changes an alert status to in-progress.
     *
     * @param description a description of the change (may be null)
     */
    Alert inProgress(String description);

    /**
     * Changes an alert status to in-progress.
     *
     * @param description a description of the change (may be null)
     * @param content     alert type-specific content associated with the state change
     */
    <C extends Serializable> Alert inProgress(String description, C content);

    /**
     * Changes an alert status to handled.
     *
     * @param description a description of the change (may be null)
     */
    Alert handle(String description);

    /**
     * Changes an alert status to handled.
     *
     * @param description a description of the change (may be null)
     * @param content     alert type-specific content associated with the state change
     */
    <C extends Serializable> Alert handle(String description, C content);

    /**
     * Changes an alert status to unhandled.
     *
     * @param description a description of the change (may be null)
     */
    Alert unhandle(String description);

    /**
     * Changes an alert status to unhandled.
     *
     * @param description a description of the change (may be null)
     * @param content     alert type-specific content associated with the state change
     */
    <C extends Serializable> Alert unhandle(String description, C content);

    /**
     * Changes an alert status to unhandled.
     *
     * @param description a description of the change (may be null)
     * @param content     alert type-specific content associated with the state change
     */
    <C extends Serializable> Alert updateAlertChange(String description, C content);

    /**
     * clears (hides or removes) an alert.  No other responder will see this alert if cleared.
     */
    void clear();


    /**
     * resets the clear flag on the alert
     */
    void unclear();
}
