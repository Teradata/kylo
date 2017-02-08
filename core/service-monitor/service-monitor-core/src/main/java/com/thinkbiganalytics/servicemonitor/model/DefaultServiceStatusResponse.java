package com.thinkbiganalytics.servicemonitor.model;

/*-
 * #%L
 * thinkbig-service-monitor-core
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * The default response and builder for {@link ServiceStatusResponse} objects.
 * This class includes a builder to help build the response object.
 * This is used by the Kylo user interface
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultServiceStatusResponse implements ServiceStatusResponse {

    List<ServiceComponent> components;
    List<ServiceComponent> healthyComponents;
    List<ServiceComponent> unhealthyComponents;
    private String serviceName;
    private Date checkDate;
    private Date latestAlertTimestamp;
    private Date earliestAlertTimestamp;
    private List<ServiceAlert> alerts;
    private STATE state;
    private List<ServiceAlert> alertsWithoutComponent;

    public DefaultServiceStatusResponse(String serviceName, List<ServiceComponent> components) {
        this.serviceName = serviceName;
        this.components = components;
        this.healthyComponents = new ArrayList<>();
        this.unhealthyComponents = new ArrayList<>();
        this.build();
    }

    public DefaultServiceStatusResponse(String serviceName, List<ServiceComponent> components, List<ServiceAlert> alerts) {
        this.serviceName = serviceName;
        this.components = components;
        this.healthyComponents = new ArrayList<>();
        this.unhealthyComponents = new ArrayList<>();
        this.alerts = alerts;
        this.build();
    }


    private void updateServiceState() {
        List<ServiceComponent.STATE> states = new ArrayList<ServiceComponent.STATE>();
        boolean hasErrorAlerts = false;
        Date latestAlertTimestamp = null;
        Date earliestAlertTimestamp = null;
        if (components != null && !components.isEmpty()) {
            for (ServiceComponent component : this.getComponents()) {

                if (StringUtils.isBlank(component.getServiceName())) {
                    component.setServiceName(serviceName);
                }

                states.add(component.getState());
                if (component.isContainsErrorAlerts()) {
                    hasErrorAlerts = true;
                }
                if (!component.isHealthy()) {
                    this.unhealthyComponents.add(component);
                } else {
                    this.healthyComponents.add(component);
                }
                Date latest = component.getLatestAlertTimestamp();
                Date earliest = component.getEarliestAlertTimestamp();
                if (latestAlertTimestamp == null || (latestAlertTimestamp != null && latest != null && latest.after(latestAlertTimestamp))) {
                    latestAlertTimestamp = component.getLatestAlertTimestamp();
                }
                if (earliestAlertTimestamp == null || (earliestAlertTimestamp != null && earliest != null && earliest.after(earliestAlertTimestamp))) {
                    earliestAlertTimestamp = component.getEarliestAlertTimestamp();
                }
            }
        }
        if (latestAlertTimestamp == null) {
            latestAlertTimestamp = new Date();
        }
        if (earliestAlertTimestamp == null) {
            earliestAlertTimestamp = new Date();
        }
        this.latestAlertTimestamp = latestAlertTimestamp;
        this.earliestAlertTimestamp = earliestAlertTimestamp;
        if (states.contains(ServiceComponent.STATE.DOWN)) {
            this.state = STATE.DOWN;
        } else if ((states.contains(ServiceComponent.STATE.UP) && hasErrorAlerts) || states.contains(ServiceComponent.STATE.UNKNOWN)) {
            this.state = STATE.WARNING;
        } else {
            this.state = STATE.UP;
        }

    }

    public void build() {
        //Change the State of the overall Service
        updateServiceState();
        this.checkDate = new Date();
        this.alertsWithoutComponent = getAlertsWithoutComponent(this.alerts);


    }


    /**
     * return a matching List of ServiceAlerts based upon the incoming component name
     */
    private List<ServiceAlert> getAlertsWithoutComponent(List<ServiceAlert> alerts) {
        if (alerts != null) {
            Predicate<ServiceAlert> predicate = new Predicate<ServiceAlert>() {
                @Override
                public boolean apply(ServiceAlert alert) {
                    return StringUtils.isBlank(alert.getComponentName()) && alert.getState().isError();
                }
            };
            Collection<ServiceAlert> matchingAlerts = Collections2.filter(alerts, predicate);
            if (matchingAlerts != null && !matchingAlerts.isEmpty()) {
                return new ArrayList<ServiceAlert>(matchingAlerts);
            } else {
                return null;
            }
        }
        return null;
    }

    public String getServiceName() {
        return serviceName;
    }

    public List<ServiceComponent> getComponents() {
        return components;
    }

    public List<ServiceComponent> getHealthyComponents() {
        return healthyComponents;
    }

    public List<ServiceComponent> getUnhealthyComponents() {
        return unhealthyComponents;
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public List<ServiceAlert> getAlerts() {
        return alerts;
    }

    public List<ServiceAlert> getAlertsWithoutComponent() {
        return alertsWithoutComponent;
    }


    public STATE getState() {
        return state;
    }

    public Date getLatestAlertTimestamp() {
        return latestAlertTimestamp;
    }

    public Date getEarliestAlertTimestamp() {
        return earliestAlertTimestamp;
    }
}
