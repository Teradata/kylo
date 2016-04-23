package com.thinkbiganalytics.servicemonitor.rest.client.cdh;

import com.cloudera.api.DataView;
import com.cloudera.api.model.ApiCluster;
import com.cloudera.api.model.ApiClusterList;
import com.cloudera.api.model.ApiHealthCheck;
import com.cloudera.api.model.ApiHealthSummary;
import com.cloudera.api.model.ApiRole;
import com.cloudera.api.model.ApiRoleList;
import com.cloudera.api.model.ApiService;
import com.cloudera.api.model.ApiServiceList;
import com.cloudera.api.v1.ClustersResource;
import com.cloudera.api.v1.RootResourceV1;

import java.util.List;

/**
 * Created by sr186054 on 10/12/15.
 */
public class DefaultClouderaRootResource implements ClouderaRootResource {

  private RootResourceV1 rootResource;

  public DefaultClouderaRootResource(RootResourceV1 rootResource) {
    this.rootResource = rootResource;
  }

  public RootResourceV1 getRootResource() {
    return this.rootResource;
  }

  public ClustersResource getClusterResource() {
    return rootResource.getClustersResource();
  }

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
            String healthSummaryName = healthSummary != null ? healthSummary.name() : "";
            List<ApiHealthCheck> healthChecks = service.getHealthChecks();
            service.setHealthChecks(healthChecks);
            service.setHealthSummary(healthSummary);
            ApiRoleList
                roles =
                rootResource.getClustersResource().getServicesResource(clusterName).getRolesResource(serviceName).readRoles();
            if (roles != null && roles.getRoles() != null) {
              service.setRoles(roles.getRoles());
              for (ApiRole role : roles.getRoles()) {
                String roleName = role.getName();
                ApiHealthSummary roleHealthSummary = role.getHealthSummary();
                List<ApiHealthCheck> roleHealthChecks = role.getHealthChecks();
                String roleHealthSummaryName = roleHealthSummary != null ? roleHealthSummary.name() : "";
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
