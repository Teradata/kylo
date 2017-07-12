package com.thinkbiganalytics.servicemonitor.check;

/*-
 * #%L
 * thinkbig-service-monitor-ambari
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

import com.google.common.collect.Collections2;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceAlert;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceStatusResponse;
import com.thinkbiganalytics.servicemonitor.model.ServiceAlert;
import com.thinkbiganalytics.servicemonitor.model.ServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;
import com.thinkbiganalytics.servicemonitor.rest.client.ambari.AmbariClient;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.Alert;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.AlertItem;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.AlertSummary;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.ServiceComponentInfoItem;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.ServiceComponentInfoSummary;
import com.thinkbiganalytics.servicemonitor.support.ServiceMonitorCheckUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.inject.Inject;

/**
 * Ambari Service bean autowired in via the ServiceStatusManager looking for this ServicesStatusCheck interface
 *
 * Users can define what services/components to track in the kylo properties file using the following format
 *
 * {SERVICE_NAME}/[{COMPONENT_NAME},{COMPONENT_NAME}],{SERVICE_NAME}... COMPONENT_NAMES are optional <p> Example
 * application.properties ambari.services.status=HIVE/[HIVE_CLIENT],HDFS - this will track the HIVE Service and just the
 * HIVE_CLIENT ambari.services.status=HDFS,HIVE,MAPREDUCE2,SQOOP - this will track all services and all components
 */
@PropertySource("classpath:/conf/ambari.properties")
public class AmbariServicesStatusCheck implements ServicesStatusCheck {

    private static final Logger LOG = LoggerFactory.getLogger(AmbariServicesStatusCheck.class);
    @Value("${ambari.services.status:#{null}}")
    private String services;


    @Inject
    private AmbariClient ambariClient;


    private void notifyServiceDown(ServiceStatusResponse serviceStatusResponse) {
        //called every time a service is marked as down/unhealthy
        LOG.debug("Ambari service is {}", serviceStatusResponse.getState());
    }

    private void notifyServiceUp(ServiceStatusResponse serviceStatusResponse) {
        //called every time a service is marked as healthy
        LOG.debug("Ambari service is {}", serviceStatusResponse.getState());
    }

    /**
     * https://github.com/apache/ambari/blob/trunk/ambari-server/docs/api/v1/host-component-resources.md
     *
     * State 	Description                                                                          <br>
     * INIT 	The initial clean state after the component is first created.                        <br>
     * INSTALLING 	In the process of installing the component.                                  <br>
     * INSTALL_FAILED 	The component install failed.                                                <br>
     * INSTALLED 	The component has been installed successfully but is not currently running.  <br>
     * STARTING 	In the process of starting the component.                                    <br>
     * STARTED 	The component has been installed and started.                                        <br>
     * STOPPING 	In the process of stopping the component.                                    <br>
     * UNINSTALLING 	In the process of uninstalling the component.                                <br>
     * UNINSTALLED 	The component has been successfully uninstalled.                             <br>
     * WIPING_OUT 	In the process of wiping out the installed component.                        <br>
     * UPGRADING 	In the process of upgrading the component.                                   <br>
     * MAINTENANCE 	The component has been marked for maintenance.                               <br>
     * UNKNOWN 	The component state can not be determined.                                           <br>
     */
    @Override
    public List<ServiceStatusResponse> healthCheck() {

        List<ServiceStatusResponse> serviceStatusResponseList = new ArrayList<>();

        //Get the Map of Services and optional Components we are tracking
        Map<String, List<String>> definedServiceComponentMap = ServiceMonitorCheckUtil.getMapOfServiceAndComponents(services);

        if (definedServiceComponentMap != null && !definedServiceComponentMap.isEmpty()) {

            try {
                AmbariClient client = ambariClient;
                //get the Clusers from ambari
                List<String> clusterNames = client.getAmbariClusterNames();
                //get the Service Status from Ambari
                ServiceComponentInfoSummary response = client.getServiceComponentInfo(clusterNames, services);
                //get alert info for these services as well
                AlertSummary alertSummary = client.getAlerts(clusterNames, services);
                //Convert the Ambari Alerts to the Pipeline Controller Alert
                List<ServiceAlert> serviceAlerts = transformAmbariAlert(alertSummary);
                //Convert the Ambari ServiceComponentInfo objects to Pipeline Controller ServiceComponents
                serviceStatusResponseList = transformAmbariServiceComponents(response, serviceAlerts, definedServiceComponentMap);
            } catch (RestClientException e) {
                Throwable cause;
                if (e.getCause() != null) {
                    cause = e.getCause();
                } else {
                    cause = e;
                }
                ServiceComponent ambariServiceComponent = new DefaultServiceComponent.Builder("Unknown", "Ambari", "Ambari REST_CLIENT", ServiceComponent.STATE.DOWN).exception(cause).build();
                List<ServiceComponent> ambariComponents = new ArrayList<>();
                ambariComponents.add(ambariServiceComponent);
                ServiceStatusResponse
                    serviceStatusResponse =
                    new DefaultServiceStatusResponse(ambariServiceComponent.getServiceName(), ambariComponents);
                serviceStatusResponseList.add(serviceStatusResponse);
                //add the other services as being Warnings
                addAmbariServiceErrors(cause.getMessage(), serviceStatusResponseList, definedServiceComponentMap);

            }
        }
        return serviceStatusResponseList;
    }


    /**
     * Convert Ambari ServiceComponentInfo into  ServiceComponent objects
     */
    private List<ServiceStatusResponse> transformAmbariServiceComponents(ServiceComponentInfoSummary ambariServiceComponents,
                                                                         List<ServiceAlert> serviceAlerts,
                                                                         Map<String, List<String>> definedServiceComponentMap) {
        List<ServiceStatusResponse> list = new ArrayList<>();
        if (ambariServiceComponents != null) {
            Map<String, List<ServiceComponent>> serviceComponentMap = new HashMap<>();

            for (ServiceComponentInfoItem item : ambariServiceComponents.getItems()) {
                ServiceComponent.STATE state = getServiceComponentState(item);
                String message = item.getServiceComponentInfo().getState();

                String name = item.getServiceComponentInfo().getComponentName();
                String serviceName = item.getServiceComponentInfo().getServiceName();
                String clusterName = item.getServiceComponentInfo().getClusterName();
                ServiceComponent
                    component =
                    new DefaultServiceComponent.Builder(clusterName, serviceName, name, state)
                        .alerts(alertsForComponent(serviceAlerts, item.getServiceComponentInfo().getComponentName())).message(message)
                        .build();

                if (!serviceComponentMap.containsKey(component.getServiceName())) {
                    serviceComponentMap.put(component.getServiceName(), new ArrayList<>());
                }

                if (definedServiceComponentMap.get(component.getServiceName()).contains(ServiceMonitorCheckUtil.ALL_COMPONENTS)
                    || definedServiceComponentMap.get(component.getServiceName()).contains(component.getName())) {
                    serviceComponentMap.get(component.getServiceName()).add(component);
                }
            }
            //build the response
            for (Map.Entry<String, List<ServiceComponent>> entry : serviceComponentMap.entrySet()) {

                List<ServiceAlert> alertsForService = alertsForService(serviceAlerts, entry.getKey());
                ServiceStatusResponse
                    serviceStatusResponse =
                    new DefaultServiceStatusResponse(entry.getKey(), entry.getValue(), alertsForService);
                if (ServiceStatusResponse.STATE.DOWN.equals(serviceStatusResponse.getState())) {
                    notifyServiceDown(serviceStatusResponse);
                } else if (ServiceStatusResponse.STATE.UP.equals(serviceStatusResponse.getState())) {
                    notifyServiceUp(serviceStatusResponse);
                }
                list.add(serviceStatusResponse);
            }

        }
        return list;
    }

    /**
     * add ambari Service errors to the supplied list
     */
    private void addAmbariServiceErrors(String exceptionMessage, List<ServiceStatusResponse> list,
                                        Map<String, List<String>> definedServiceComponentMap) {
        if (definedServiceComponentMap != null && !definedServiceComponentMap.isEmpty()) {
            String message = "Status Unknown. Unable to check service.  Ambari connection error: " + exceptionMessage;
            for (Map.Entry<String, List<String>> entry : definedServiceComponentMap.entrySet()) {
                String serviceName = entry.getKey();
                List<String> componentNames = entry.getValue();
                List<ServiceComponent> components = new ArrayList<>();
                if (componentNames != null && !componentNames.isEmpty()) {
                    for (String componentName : componentNames) {
                        if (ServiceMonitorCheckUtil.ALL_COMPONENTS.equals(componentName)) {
                            componentName = serviceName;
                        }
                        ServiceComponent
                            serviceComponent =
                            new DefaultServiceComponent.Builder("Unknown", serviceName, componentName, ServiceComponent.STATE.UNKNOWN)
                                .message(message).build();
                        components.add(serviceComponent);
                    }
                } else {
                    //add the component based uppon the Service Name
                    ServiceComponent
                        serviceComponent =
                        new DefaultServiceComponent.Builder("Unknown", serviceName, serviceName, ServiceComponent.STATE.UNKNOWN)
                            .message(message).build();
                    components.add(serviceComponent);
                }
                ServiceStatusResponse serviceStatusResponse = new DefaultServiceStatusResponse(serviceName, components);
                list.add(serviceStatusResponse);

            }
        }
    }

    /**
     * for a given Ambari component and state return the respective Component state
     *
     * @return the state of the component
     */
    private ServiceComponent.STATE getServiceComponentState(ServiceComponentInfoItem serviceComponentInfoItem) {
        ServiceComponent.STATE state = ServiceComponent.STATE.DOWN;
        serviceComponentInfoItem.updateServiceComponentInfoState();
        String ambariState = serviceComponentInfoItem.getServiceComponentInfo().getState();
        //check for category
        boolean isClient = serviceComponentInfoItem.getServiceComponentInfo().getCategory().equalsIgnoreCase("CLIENT");
        if (isClient && "INSTALLED".equals(ambariState)) {
            state = ServiceComponent.STATE.UP;
        } else {
            if ("STARTING".equalsIgnoreCase(ambariState)) {
                state = ServiceComponent.STATE.STARTING;
            } else if ("STARTED".equalsIgnoreCase(ambariState)) {
                state = ServiceComponent.STATE.UP;
            } else if ("UNKNOWN".equalsIgnoreCase(ambariState)) {
                state = ServiceComponent.STATE.UNKNOWN;
            }
        }
        return state;
    }

    /**
     * return a matching List of ServiceAlerts based upon the incoming component name
     */
    private List<ServiceAlert> alertsForComponent(List<ServiceAlert> alerts, final String component) {
        Predicate<ServiceAlert> predicate = alert -> alert.getComponentName() != null && alert.getComponentName().equals(component);
        Collection<ServiceAlert> matchingAlerts = Collections2.filter(alerts, predicate::test);
        if (matchingAlerts != null && !matchingAlerts.isEmpty()) {
            return new ArrayList<>(matchingAlerts);
        }
        return null;
    }

    /**
     * get the list of alerts for a give service name
     */
    private List<ServiceAlert> alertsForService(List<ServiceAlert> alerts, final String service) {
        Predicate<ServiceAlert> predicate = alert -> alert.getServiceName() != null && alert.getServiceName().equals(service);
        Collection<ServiceAlert> matchingAlerts = Collections2.filter(alerts, predicate::test);
        if (matchingAlerts != null && !matchingAlerts.isEmpty()) {
            return new ArrayList<>(matchingAlerts);
        }
        return null;
    }


    /**
     * Transform the ambari alerts to Kylo service alerts
     */
    private List<ServiceAlert> transformAmbariAlert(AlertSummary alertSummary) {
        List<ServiceAlert> serviceAlerts = new ArrayList<>();
        if (alertSummary != null) {
            List<AlertItem> alertItems = alertSummary.getItems();
            if (alertItems != null) {
                for (AlertItem alertItem : alertItems) {
                    Alert alert = alertItem.getAlert();
                    ServiceAlert serviceAlert = new DefaultServiceAlert();
                    serviceAlert.setServiceName(alertItem.getAlert().getServiceName());
                    serviceAlert.setComponentName(alert.getComponentName());
                    serviceAlert.setFirstTimestamp(new Date(alert.getOriginalTimestamp()));
                    serviceAlert.setLatestTimestamp(new Date(alert.getLatestTimestamp()));
                    serviceAlert.setLabel(alert.getLabel());
                    serviceAlert.setMessage(alert.getText());
                    if (StringUtils.isNotBlank(alert.getState())) {
                        try {
                            serviceAlert.setState(ServiceAlert.STATE.valueOf(alert.getState()));
                        } catch (IllegalArgumentException e) {
                            serviceAlert.setState(ServiceAlert.STATE.UNKNOWN);
                        }
                    } else {
                        serviceAlert.setState(ServiceAlert.STATE.UNKNOWN);
                    }

                    serviceAlerts.add(serviceAlert);
                }
            }
        }
        return serviceAlerts;
    }

    protected void setServices(String services) {
        this.services = services;
    }
}
