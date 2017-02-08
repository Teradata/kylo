package com.thinkbiganalytics.servicemonitor.model;

/*-
 * #%L
 * thinkbig-service-monitor-api
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

import java.util.Date;

/**
 * Health information for a service
 * A Service  can be configured to have a number of Components which it should check for health.
 * Each component can provide a number of ServiceAlert indicating the overall healh of the given component
 *
 * Refer to the service-monitor-core for builder objects to create these alerts
 */
public interface ServiceAlert {

    /**
     * The Service Name
     */
    String getServiceName();

    void setServiceName(String serviceName);

    /**
     * The name of the component for this alert
     */
    String getComponentName();

    void setComponentName(String componentName);

    /**
     * The label shown on the service component -> alert details page
     */
    String getLabel();

    /**
     * Set the label for this alert
     */
    void setLabel(String label);

    String getMessage();

    void setMessage(String message);

    Date getFirstTimestamp();

    void setFirstTimestamp(Date firstTimestamp);

    Date getLatestTimestamp();

    void setLatestTimestamp(Date latestTimestamp);

    STATE getState();

    void setState(STATE state);

    public enum STATE {
        OK(1), UNKNOWN(2), WARNING(3), CRITICAL(4);
        private int severity;

        STATE(int severity) {
            this.severity = severity;
        }

        public int getSeverity() {
            return this.severity;
        }

        public boolean isError() {
            return this.severity > 1;
        }

        public boolean isHealthy() {
            return this.severity == 1;
        }
    }
}
