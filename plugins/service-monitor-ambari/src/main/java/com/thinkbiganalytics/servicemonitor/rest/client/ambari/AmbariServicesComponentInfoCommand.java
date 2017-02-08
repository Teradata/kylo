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

import com.thinkbiganalytics.servicemonitor.rest.model.ambari.ServiceComponentInfoSummary;
import com.thinkbiganalytics.servicemonitor.support.ServiceMonitorCheckUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A command to get component status of a given cluster.
 */
public class AmbariServicesComponentInfoCommand extends AmbariServiceCheckRestCommand<ServiceComponentInfoSummary> {

    /**
     * @param clusterName cluster name for which services are to be checked
     * @param services    list of services to be checked for given cluster
     */
    AmbariServicesComponentInfoCommand(String clusterName, String services) {
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
