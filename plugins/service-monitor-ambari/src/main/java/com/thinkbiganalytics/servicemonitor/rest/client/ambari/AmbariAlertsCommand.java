package com.thinkbiganalytics.servicemonitor.rest.client.ambari;

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
