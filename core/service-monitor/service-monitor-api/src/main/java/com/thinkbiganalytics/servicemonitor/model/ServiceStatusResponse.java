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

/**
 * When a Service Status check is made it expects this object to be returned.
 * This includes the Service and respective {@link ServiceComponent} and {@link ServiceAlert} objects as well as information to quickly assess if the service is healthy or not
 *
 * Refer to the service-monitor-core for common builders in creating this object
 */
public interface ServiceStatusResponse {

    String getServiceName();

    List<ServiceComponent> getComponents();

    List<ServiceComponent> getHealthyComponents();

    List<ServiceComponent> getUnhealthyComponents();

    Date getCheckDate();

    List<ServiceAlert> getAlerts();

    List<ServiceAlert> getAlertsWithoutComponent();

    STATE getState();

    Date getLatestAlertTimestamp();

    Date getEarliestAlertTimestamp();

    public enum STATE {
        UP, DOWN, WARNING;
    }
}
