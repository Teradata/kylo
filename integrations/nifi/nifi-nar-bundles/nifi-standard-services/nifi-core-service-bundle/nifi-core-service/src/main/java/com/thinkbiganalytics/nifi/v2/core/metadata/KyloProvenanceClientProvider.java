package com.thinkbiganalytics.nifi.v2.core.metadata;

/*-
 * #%L
 * thinkbig-nifi-core-service
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

import com.thinkbiganalytics.metadata.rest.client.MetadataClient;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;
import com.thinkbiganalytics.nifi.core.api.metadata.KyloNiFiFlowProvider;

/**
 * an implementation of KyloNifiFlowProvider, used to interact with a cache of nifi flow events
 */
public class KyloProvenanceClientProvider implements KyloNiFiFlowProvider {

    private MetadataClient client;


    /**
     * constructor which requires a client
     *
     * @param client the metadata client
     */
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

    @Override
    public boolean isNiFiFlowDataAvailable() {
        return client.isNiFiFlowDataAvailable();
    }

    public Long resetNiFiMaxEventId(String clusterNodeId){
        return client.resetNiFiMaxEventId(clusterNodeId);
    }

}
