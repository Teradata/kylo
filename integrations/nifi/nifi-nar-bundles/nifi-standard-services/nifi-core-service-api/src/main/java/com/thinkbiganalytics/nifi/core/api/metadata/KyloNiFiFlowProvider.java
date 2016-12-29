package com.thinkbiganalytics.nifi.core.api.metadata;

import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;

/**
 * Created by sr186054 on 12/21/16.
 */
public interface KyloNiFiFlowProvider {


    NiFiFlowCacheSync getNiFiFlowUpdates(String syncId);

    NiFiFlowCacheSync resetNiFiFlowCache(String syncId);

    boolean isNiFiFlowDataAvailable();

}
