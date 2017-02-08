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
import java.util.List;
import java.util.Map;

/**
 * A service can monitor 1 or more components.
 * Each component has its own health status along with the overal Service health status
 */
public interface ServiceComponent {

    String DEFAULT_CLUSTER = "Default";

    /**
     * Returns the Highest/Most severe alert state for alerts found on this component
     */
    ServiceAlert.STATE getHighestAlertState();

    /**
     * Get any additional properties to display on the Kylo UI for the service component
     */
    Map<String, Object> getProperties();

    /**
     * @return the associated service name for this component
     */
    String getServiceName();

    void setServiceName(String serviceName);

    /**
     * @return the name for this component
     */
    String getName();

    /**
     * @return true if healthy, false if unhealthy
     */
    boolean isHealthy();

    /**
     * @return the date last checked
     */
    Date getCheckDate();

    /**
     * @return a message indicating some description of this component and alert status
     */
    String getMessage();

    /**
     * Get the state of this component
     *
     * @return the state of the component
     */
    STATE getState();

    /**
     * Get associated alerts for this component
     *
     * @return a list of alert objects
     */
    List<ServiceAlert> getAlerts();

    /**
     * if there are any error alerts
     */
    boolean isContainsErrorAlerts();

    /**
     * if associated with a cluster, provide the cluster name the component is on
     *
     * @return the name of the cluster
     */
    String getClusterName();

    /**
     * find any error alerts
     *
     * @return a list of alerts marked as errors
     */
    List<ServiceAlert> getErrorAlerts();

    /**
     * find the latest alert time for this component
     *
     * @return the latest timestamp for this alert
     */
    Date getLatestAlertTimestamp();

    /**
     * find the earliest alert time for the list of alerts on this component
     *
     * @return the earliest date for the alerts on this component
     */
    Date getEarliestAlertTimestamp();

    /**
     * Return the most severe alert state
     */
    ServiceAlert.STATE getMaxAlertState();


    /**
     * The state of a given Component
     */
    public enum STATE {
        UP(1), STARTING(2), DOWN(3), UNKNOWN(4);
        private int severity;

        STATE(int severity) {
            this.severity = severity;
        }

        public int getSeverity() {
            return this.severity;
        }

        public boolean isError() {
            return this.severity > 2;
        }

        public boolean isHealthy() {
            return this.severity <= 2;
        }
    }

    public enum TIMESTAMP_TYPE {
        EARLIEST, LATEST
    }
}
