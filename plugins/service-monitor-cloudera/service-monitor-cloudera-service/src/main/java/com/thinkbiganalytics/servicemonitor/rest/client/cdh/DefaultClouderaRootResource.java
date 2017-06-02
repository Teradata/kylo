package com.thinkbiganalytics.servicemonitor.rest.client.cdh;

/*-
 * #%L
 * thinkbig-service-monitor-cloudera
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

import com.cloudera.api.DataView;
import com.cloudera.api.model.ApiCluster;
import com.cloudera.api.model.ApiClusterList;
import com.cloudera.api.model.ApiHealthCheck;
import com.cloudera.api.model.ApiHealthSummary;
import com.cloudera.api.model.ApiRole;
import com.cloudera.api.model.ApiRoleList;
import com.cloudera.api.model.ApiService;
import com.cloudera.api.model.ApiServiceList;
import com.cloudera.api.v1.RootResourceV1;

import java.util.List;

/**
 * Default implementation of ClouderaRootResource which knows how to
 * initialise service health status.
 */
public class DefaultClouderaRootResource implements ClouderaRootResource {

    private RootResourceV1 rootResource;

    DefaultClouderaRootResource(RootResourceV1 rootResource) {
        this.rootResource = rootResource;
    }

    /**
     * @return cluster list with initialised service health status
     */
    public ApiClusterList getPopulatedClusterList() {
        ApiClusterList clusters = rootResource.getClustersResource().readClusters(DataView.SUMMARY);
        if (clusters != null && clusters.getClusters() != null) {
            for (ApiCluster cluster : clusters.getClusters()) {
                String clusterName = cluster.getName();
                ApiServiceList
                    services =
                    rootResource.getClustersResource().getServicesResource(clusterName).readServices(DataView.SUMMARY);
                if (services != null && services.getServices() != null) {
                    cluster.setServices(services.getServices());
                    for (ApiService service : services.getServices()) {
                        String serviceName = service.getName();
                        ApiHealthSummary healthSummary = service.getHealthSummary();
                        List<ApiHealthCheck> healthChecks = service.getHealthChecks();
                        service.setHealthChecks(healthChecks);
                        service.setHealthSummary(healthSummary);
                        ApiRoleList
                            roles =
                            rootResource.getClustersResource().getServicesResource(clusterName).getRolesResource(serviceName).readRoles();
                        if (roles != null && roles.getRoles() != null) {
                            service.setRoles(roles.getRoles());
                            for (ApiRole role : roles.getRoles()) {
                                ApiHealthSummary roleHealthSummary = role.getHealthSummary();
                                List<ApiHealthCheck> roleHealthChecks = role.getHealthChecks();
                                role.setHealthSummary(roleHealthSummary);
                                role.setHealthChecks(roleHealthChecks);
                            }
                        }
                    }
                }
            }
        }
        return clusters;
    }
}
