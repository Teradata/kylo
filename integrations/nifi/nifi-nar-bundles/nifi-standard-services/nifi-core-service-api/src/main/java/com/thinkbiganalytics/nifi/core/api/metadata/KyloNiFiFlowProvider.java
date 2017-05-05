package com.thinkbiganalytics.nifi.core.api.metadata;

/*-
 * #%L
 * thinkbig-nifi-core-service-api
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

import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;

/**
 * Used to interact with a cache of nifi flow events
 */
public interface KyloNiFiFlowProvider {

    /**
     * Gets the nifi flow data since syncId
     *
     * @param syncId the id of the last flow
     * @return a cache of new flow data
     */
    NiFiFlowCacheSync getNiFiFlowUpdates(String syncId);

    /**
     * Resets the cache from the syncId
     *
     * @param syncId the id of the flow
     * @return a cache that has been reset
     */
    NiFiFlowCacheSync resetNiFiFlowCache(String syncId);

    /**
     * check to see if new data is available in the nifi flow
     *
     * @return true if new data is available
     */
    boolean isNiFiFlowDataAvailable();

    /**
     * Returns the max event ID for a given cluster
     *
     * @param clusterNodeId the cluster to search for the max event id
     * @return the event id
     */
    Long findNiFiMaxEventId(String clusterNodeId);

    Long resetNiFiMaxEventId(String clusterNodeId);

}
