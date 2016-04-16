package com.thinkbiganalytics.servicemonitor.rest.client.ambari;

import com.thinkbiganalytics.servicemonitor.rest.model.ambari.ServiceComponentInfoSummary;
import com.thinkbiganalytics.servicemonitor.support.ServiceMonitorCheckUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 10/1/15.
 */
public class AmbariServicesComponentInfoCommand extends AmbariServiceCheckRestCommand<ServiceComponentInfoSummary> {

  public AmbariServicesComponentInfoCommand(String clusterName, String services) {
    super(clusterName, services);
  }

  @Override
  public String payload() {
    return null;
  }

  @Override
  public String getUrl() {
    return "clusters/" + getClusterName() + "/components/";
  }

  @Override
  public Map<String, Object> getParameters() {
    Map<String, Object> params = new HashMap<>();
    StringBuilder servicesString = null;
    List<String> serviceList = ServiceMonitorCheckUtil.getServiceNames(this.getServices());
    if (serviceList != null) {
      for (String service : serviceList) {
        if (servicesString == null) {
          servicesString = new StringBuilder();
        } else {
          servicesString.append("|");
          servicesString.append("ServiceComponentInfo/service_name=");
        }
        servicesString.append(service);
      }
      if (servicesString != null) {
        params.put("ServiceComponentInfo/service_name", servicesString.toString());
      }
      params.put("fields",
                 "ServiceComponentInfo/Version,ServiceComponentInfo/category,ServiceComponentInfo/StartTime,ServiceComponentInfo/service_name,host_components/HostRoles/host_name,host_components/HostRoles/state");
      params.put("minimal_response", true);
    }
    return params;
  }

}
