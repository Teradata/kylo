package com.thinkbiganalytics.servicemonitor.rest.client.ambari;


import com.thinkbiganalytics.servicemonitor.rest.model.ambari.AlertSummary;
import com.thinkbiganalytics.servicemonitor.support.ServiceMonitorCheckUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 10/2/15.
 */

public class AmbariAlertsCommand extends AmbariServiceCheckRestCommand<AlertSummary> {
//http://localhost:8080/api/v1/clusters/Sandbox/alerts?fields=*&Alert/service_name.in%28HDFS%29

  List<String> serviceList;
  Map<String, Object> parameters;
  StringBuffer sb = new StringBuffer();

  public String getPathString() {
    return "?" + sb.toString();
  }

  public AmbariAlertsCommand(String clusterName, String services) {
    super(clusterName, services);
  }

  @Override
  public String payload() {
    return null;
  }

  @Override
  public void beforeRestRequest() {
    sb = new StringBuffer();
    super.beforeRestRequest();
    serviceList = ServiceMonitorCheckUtil.getServiceNames(this.getServices());
    String serviceString = StringUtils.join(serviceList, ",");
    parameters = new HashMap<>();
    sb.append("fields=*");
    sb.append("&Alert/service_name.in(" + serviceString + ")");
  }

  @Override
  public String getUrl() {
    return "clusters/" + getClusterName() + "/alerts";
  }

  @Override
  public Map<String, Object> getParameters() {
    return parameters;
  }

}
