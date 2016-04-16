package com.thinkbiganalytics.servicemonitor.rest.client.ambari;

import com.thinkbiganalytics.servicemonitor.rest.client.RestCommand;
import com.thinkbiganalytics.servicemonitor.rest.model.ambari.ClusterList;

import java.util.Map;

/**
 * Created by sr186054 on 10/2/15.
 */

public class AmbariGetClustersCommand extends RestCommand<ClusterList> {

    @Override
    public String payload() {
        return null;
    }

    @Override
    public String getUrl() {
        return "/clusters";
    }

    @Override
    public Map<String, Object> getParameters() {
       return null;
    }
}
