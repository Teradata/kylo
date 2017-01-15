package com.thinkbiganalytics.nifi.v2.core.metadata;

import com.thinkbiganalytics.metadata.rest.client.MetadataClient;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;
import com.thinkbiganalytics.nifi.core.api.metadata.KyloNiFiFlowProvider;

/**
 * Created by sr186054 on 12/21/16.
 */
public class KyloProvenanceClientProvider implements KyloNiFiFlowProvider {

    private MetadataClient client;


    public KyloProvenanceClientProvider(MetadataClient client) {
        super();
        this.client = client;
    }


    @Override
    public NiFiFlowCacheSync getNiFiFlowUpdates(String syncId) {
        return client.getFlowUpdates(syncId);
    }

    @Override
    public NiFiFlowCacheSync resetNiFiFlowCache(String syncId) {
        return client.resetFlowUpdates(syncId);
    }

    @Override
    public Long findNiFiMaxEventId(String clusterNodeId) {
        return client.findNiFiMaxEventId(clusterNodeId);
    }

    public boolean isNiFiFlowDataAvailable() {
        return client.isNiFiFlowDataAvailable();
    }

}
