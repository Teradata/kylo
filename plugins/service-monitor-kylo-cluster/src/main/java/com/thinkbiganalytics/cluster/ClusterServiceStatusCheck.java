package com.thinkbiganalytics.cluster;

/*-
 * #%L
 * kylo-service-monitor-kylo-cluster
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

import com.thinkbiganalytics.servicemonitor.check.ServiceStatusCheck;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceAlert;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.DefaultServiceStatusResponse;
import com.thinkbiganalytics.servicemonitor.model.ServiceAlert;
import com.thinkbiganalytics.servicemonitor.model.ServiceComponent;
import com.thinkbiganalytics.servicemonitor.model.ServiceStatusResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Monitor the status of the kylo cluster
 */
public class ClusterServiceStatusCheck implements ServiceStatusCheck {

    @Value("${kylo.cluster.nodeCount:#{null}}")
    private String expectedNodes;

    @Inject
    private ClusterService clusterService;

    public ServiceStatusResponse healthCheck() {

        String serviceName = "Kylo Cluster";
        String MEMBER_COUNT_PROPERTY = "member count";
        String MEMBERS_PROPERTY = "members";

        Map<String, Object> properties = new HashMap<>();
        boolean isClustered = clusterService.isClustered();

        boolean valid = true;
        List<ServiceComponent> components = new ArrayList<>();
        String serviceMessage = "";
        List<ServiceAlert> serviceAlerts = new ArrayList<>();
        if(isClustered) {
            int memberCount = clusterService.getMembersAsString().size();
            List<String> members = clusterService.getMembersAsString();

            if(StringUtils.isNotBlank(expectedNodes)) {
                try {
                    int expectedNodeCount = Integer.valueOf(expectedNodes);
                    if (expectedNodeCount != memberCount) {
                        valid = false;
                        serviceMessage =
                            "Error.  Missing " + (expectedNodeCount - memberCount) + " nodes. ";

                    } else {
                        serviceMessage = " All " + expectedNodeCount + " nodes are connected. " ;
                    }
                } catch (NumberFormatException e) {
                    valid = false;
                    serviceMessage = "  Unable to validate the expected kylo node count.  Ensure the 'kylo.cluster.nodeCount' property is set to a valid number.";
                }
            }
            final boolean isValid = valid;
            final String finalServiceMessage = serviceMessage;

            members.stream().forEach(member -> {
              String  componentName = member;
              boolean currentlyConnected = member.equalsIgnoreCase(clusterService.getAddressAsString());
                properties.put(MEMBER_COUNT_PROPERTY,memberCount);
                properties.put(MEMBERS_PROPERTY,members.toString());

               String message = "There are "+memberCount+" members in the cluster. ";
               if(currentlyConnected){
                   message = "Currently connected to this node. "+message;
               }
                ServiceAlert alert = null;
               if(isValid){
                   message = finalServiceMessage + message;
               }else {
                  alert = new DefaultServiceAlert();
                  alert.setLabel(componentName);
                   alert.setServiceName(serviceName);
                   alert.setComponentName(componentName);
                   alert.setMessage(finalServiceMessage);
                   alert.setState(ServiceAlert.STATE.CRITICAL);
               }

                ServiceComponent component =
                    new DefaultServiceComponent.Builder(componentName, isValid ? ServiceComponent.STATE.UP: ServiceComponent.STATE.DOWN)
                        .message(message).properties(properties).addAlert(alert).build();

                components.add(component);

            });

        }
        else {
            serviceMessage ="Kylo is not configured to be running as a cluster";
            ServiceComponent component =
                new DefaultServiceComponent.Builder(serviceName,  ServiceComponent.STATE.UP)
                    .message(serviceMessage).properties(properties).build();
            components.add(component);
        }




        return new DefaultServiceStatusResponse(serviceName, components);
    }

}
