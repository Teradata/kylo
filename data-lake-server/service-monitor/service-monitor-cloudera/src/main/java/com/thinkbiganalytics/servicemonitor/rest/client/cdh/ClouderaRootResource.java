package com.thinkbiganalytics.servicemonitor.rest.client.cdh;

import com.cloudera.api.model.ApiClusterList;
import com.cloudera.api.v1.ClustersResource;
import com.cloudera.api.v1.RootResourceV1;

/**
 * Created by sr186054 on 10/8/15.
 */
public interface ClouderaRootResource {
    RootResourceV1 getRootResource();

    ClustersResource getClusterResource();

    ApiClusterList getPopulatedClusterList();
}
