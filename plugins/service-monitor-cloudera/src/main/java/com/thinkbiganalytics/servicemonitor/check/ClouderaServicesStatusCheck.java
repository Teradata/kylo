package com.thinkbiganalytics.servicemonitor.check;

import com.cloudera.api.model.ApiCluster;
import com.cloudera.api.model.ApiClusterList;
import com.cloudera.api.model.ApiHealthCheck;
import com.cloudera.api.model.ApiHealthSummary;
import com.cloudera.api.model.ApiRole;
import com.cloudera.api.model.ApiRoleState;
import com.cloudera.api.model.ApiService;
import com.cloudera.api.model.ApiServiceState;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceAlert;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceStatusResponse;
import com.thinkbiganalytics.servicemonitor.model.ServiceAlert;
import com.thinkbiganalytics.servicemonitor.model.ServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;
import com.thinkbiganalytics.servicemonitor.rest.client.cdh.ClouderaClient;
import com.thinkbiganalytics.servicemonitor.rest.client.cdh.ClouderaRootResource;
import com.thinkbiganalytics.servicemonitor.support.ServiceMonitorCheckUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClouderaServicesStatusCheck implements ServicesStatusCheck {

  private static final Logger LOG = LoggerFactory.getLogger(ClouderaServicesStatusCheck.class);
  @Value("${cloudera.services.status:#{null}}")
  private String services;

  @Autowired
  private ClouderaClient clouderaClient;

  private ServiceComponent.STATE getServiceState(ApiServiceState clouderaState) {
    ServiceComponent.STATE state = ServiceComponent.STATE.DOWN;
    if (ApiServiceState.STARTED.equals(clouderaState)) {
      state = ServiceComponent.STATE.UP;
    } else if (ApiServiceState.STARTING.equals(clouderaState)) {
      state = ServiceComponent.STATE.STARTING;
    } else if (ApiServiceState.STOPPING.equals(clouderaState)) {
      state = ServiceComponent.STATE.DOWN;
    } else if (ApiServiceState.STOPPED.equals(clouderaState)) {
      state = ServiceComponent.STATE.DOWN;
    } else if (ApiServiceState.UNKNOWN.equals(clouderaState)) {
      state = ServiceComponent.STATE.UNKNOWN;
    } else {
      state = ServiceComponent.STATE.UNKNOWN;
    }
    return state;
  }

  private ServiceComponent.STATE apiRoleStateToServiceComponentState(ApiRoleState roleState) {
    ServiceComponent.STATE state = ServiceComponent.STATE.DOWN;
    if (ApiRoleState.NA.equals(roleState) || ApiRoleState.HISTORY_NOT_AVAILABLE.equals(roleState) || ApiRoleState.UNKNOWN
        .equals(roleState)) {
      state = ServiceComponent.STATE.UNKNOWN;
    }

    if (ApiRoleState.STARTED.equals(roleState)) {
      state = ServiceComponent.STATE.UP;
    } else if (ApiRoleState.STARTING.equals(roleState)) {
      state = ServiceComponent.STATE.STARTING;
    } else if (ApiRoleState.STOPPING.equals(roleState)) {
      state = ServiceComponent.STATE.DOWN;
    } else if (ApiRoleState.STOPPED.equals(roleState)) {
      state = ServiceComponent.STATE.DOWN;
    } else if (ApiRoleState.BUSY.equals(roleState)) {
      state = ServiceComponent.STATE.UP;
    } else {
      state = ServiceComponent.STATE.UNKNOWN;
    }
    return state;
  }

  private ServiceAlert.STATE apiHealthSummaryAlertState(ApiHealthSummary healthSummary) {
    ServiceAlert.STATE state = ServiceAlert.STATE.UNKNOWN;
    if (ApiHealthSummary.DISABLED.equals(healthSummary) || ApiHealthSummary.HISTORY_NOT_AVAILABLE.equals(healthSummary)
        || ApiHealthSummary.NOT_AVAILABLE.equals(healthSummary)) {
      state = ServiceAlert.STATE.UNKNOWN;
    } else if (ApiHealthSummary.GOOD.equals(healthSummary)) {
      state = ServiceAlert.STATE.OK;
    } else if (ApiHealthSummary.BAD.equals(healthSummary)) {
      state = ServiceAlert.STATE.CRITICAL;
    } else if (ApiHealthSummary.CONCERNING.equals(healthSummary)) {
      state = ServiceAlert.STATE.WARNING;
    }
    return state;
  }

  private ServiceAlert apiHealthCheckToServiceAlert(String serviceName, String componentName, ApiHealthCheck healthCheck) {
    ServiceAlert alert = new DefaultServiceAlert();
    alert.setComponentName(componentName);
    alert.setLabel(healthCheck.getName());
    alert.setMessage(healthCheck.getSummary().name());
    alert.setState(apiHealthSummaryAlertState(healthCheck.getSummary()));
    return alert;
  }

  @Override
  public List<ServiceStatusResponse> healthCheck() {

    List<ServiceStatusResponse> serviceStatusResponseList = new ArrayList<>();

    Map<String, List<ApiService>> serviceMap = new HashMap<String, List<ApiService>>();

    //Get the Map of Services and optional Components we are tracking
    Map<String, List<String>> definedServiceComponentMap = ServiceMonitorCheckUtil.getMapOfServiceAndComponents(services);

    if (definedServiceComponentMap != null && !definedServiceComponentMap.isEmpty()) {
      ClouderaRootResource rootResource = null;
      try {
        rootResource = clouderaClient.getClouderaResource();
        if(rootResource == null){
          LOG.info(" Returning 0 services.  The Cloudera Resource is null... It may still be trying to initialize the Rest Client.");
          return serviceStatusResponseList;
        }

        ApiClusterList clusters = rootResource.getPopulatedClusterList();
        for (ApiCluster cluster : clusters.getClusters()) {
          String clusterName = cluster.getName();
          List<ApiService> services = cluster.getServices();
          for (ApiService service : services) {
            List<ServiceComponent> serviceComponents = new ArrayList<>();
            List<ServiceAlert> alerts = new ArrayList<>();
            String serviceName = service.getType();
            if (definedServiceComponentMap.containsKey(serviceName)) {
              ApiHealthSummary healthSummary = service.getHealthSummary();
              List<ApiHealthCheck> healthChecks = service.getHealthChecks();

              for (ApiHealthCheck healthCheck : healthChecks) {
                alerts.add(apiHealthCheckToServiceAlert(serviceName, null, healthCheck));
              }
              List<ApiRole> roles = service.getRoles();
              List<ServiceComponent> components = new ArrayList<>();
              for (ApiRole role : roles) {
                String roleName = role.getType();
                ApiHealthSummary roleHealthSummary = role.getHealthSummary();
                List<ApiHealthCheck> roleHealthChecks = role.getHealthChecks();
                ServiceComponent.STATE roleState = apiRoleStateToServiceComponentState(role.getRoleState());
                List<ServiceAlert> componentAlerts = new ArrayList<>();
                for (ApiHealthCheck healthCheck : roleHealthChecks) {
                  ServiceAlert alert = apiHealthCheckToServiceAlert(serviceName, roleName, healthCheck);
                  alerts.add(alert);
                  componentAlerts.add(alert);
                }

                ServiceComponent
                    component =
                    new DefaultServiceComponent.Builder(roleName, roleState).clusterName(clusterName)
                            .message(role.getRoleState().name()).alerts(componentAlerts).build();
                if (definedServiceComponentMap.containsKey(serviceName) && (definedServiceComponentMap.get(serviceName).contains(
                    ServiceMonitorCheckUtil.ALL_COMPONENTS) || definedServiceComponentMap.get(serviceName)
                                                                                .contains(component.getName()))) {
                  serviceComponents.add(component);
                }

              }
              ServiceStatusResponse
                  serviceStatusResponse =
                  new DefaultServiceStatusResponse(serviceName, serviceComponents, alerts);
              serviceStatusResponseList.add(serviceStatusResponse);
            }
          }
        }
      } catch (Exception e) {
        Throwable cause;
        if (e.getCause() != null) {
          cause = e.getCause();
        } else {
          cause = e;
        }
        ServiceComponent
            clouderaServiceComponent =
            new DefaultServiceComponent.Builder("Cloudera REST_CLIENT", ServiceComponent.STATE.DOWN).serviceName("Cloudera")
                .clusterName("UNKNOWN").exception(cause).build();
        List<ServiceComponent> clouderaComponents = new ArrayList<>();
        clouderaComponents.add(clouderaServiceComponent);
        ServiceStatusResponse
            serviceStatusResponse =
            new DefaultServiceStatusResponse(clouderaServiceComponent.getServiceName(), clouderaComponents);
        serviceStatusResponseList.add(serviceStatusResponse);
        //add the other services as being Warnings
        adddClouderaServiceErrors(cause.getMessage(), serviceStatusResponseList, definedServiceComponentMap);

      }
    }

    return serviceStatusResponseList;
  }


  private void adddClouderaServiceErrors(String exceptionMessage, List<ServiceStatusResponse> list,
                                         Map<String, List<String>> definedServiceComponentMap) {
    if (definedServiceComponentMap != null && !definedServiceComponentMap.isEmpty()) {
      String message = "Status Unknown. Unable to check service.  Cloudera connection error: " + exceptionMessage;
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
                new DefaultServiceComponent.Builder(componentName, ServiceComponent.STATE.UNKNOWN).clusterName("UNKNOWN")
                    .message(message).build();
            components.add(serviceComponent);
          }
        } else {
          //add the component based uppon the Service Name
          ServiceComponent
              serviceComponent =
              new DefaultServiceComponent.Builder(serviceName, ServiceComponent.STATE.UNKNOWN).clusterName("UNKNOWN")
                  .message(message).build();
          components.add(serviceComponent);
        }
        ServiceStatusResponse serviceStatusResponse = new DefaultServiceStatusResponse(serviceName, components);
        list.add(serviceStatusResponse);

      }
    }
  }

  /**
   * Setters for unit testing purposes
   */

  public void setClouderaClient(ClouderaClient clouderaClient) {
    this.clouderaClient = clouderaClient;
  }

  protected void setServices(String services) {
    this.services = services;
  }

}
