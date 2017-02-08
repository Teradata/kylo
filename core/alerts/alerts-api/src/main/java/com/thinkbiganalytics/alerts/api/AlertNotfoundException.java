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
 *
 */
public class AlertNotfoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Alert.ID alertId;

    public AlertNotfoundException(Alert.ID id) {
        this(id, "An alert with the given ID does not exists: " + id);
    }

    public AlertNotfoundException(Alert.ID id, String message) {
        super(message);
        this.alertId = id;
    }

    /**
     * @return the alertId
     */
    public Alert.ID getAlertId() {
        return alertId;
    }
}
