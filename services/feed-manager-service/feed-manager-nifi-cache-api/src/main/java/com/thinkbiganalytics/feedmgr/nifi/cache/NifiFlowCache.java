package com.thinkbiganalytics.feedmgr.nifi.cache;

/*-
 * #%L
 * kylo-feed-manager-nifi-cache-api
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

import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;
import com.thinkbiganalytics.metadata.rest.model.nifi.NifiFlowCacheSnapshot;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.visitor.NifiVisitableProcessGroup;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;

import java.util.Collection;

/**
 * Created by sr186054 on 5/30/17.
 */
public interface NifiFlowCache {

    boolean isAvailable();

    boolean isKyloClustered();

    boolean needsUpdateFromCluster();

    void applyClusterUpdates();

    boolean isConnectedToNiFi();

    NifiFlowCacheSnapshot getLatest();

    NiFiFlowCacheSync syncAndReturnUpdates(String syncId);

    NiFiFlowCacheSync getCache(String syncId);

    NiFiFlowCacheSync previewUpdates(String syncId);

    boolean rebuildAll();

    NiFiFlowCacheSync refreshAll(String syncId);

    @Deprecated
    void updateFlow(FeedMetadata feedMetadata, NifiFlowProcessGroup feedProcessGroup);

    void updateFlowForFeed(FeedMetadata feed, String feedProcessGroupId, Collection<ProcessorDTO> processorDTOs, Collection<ConnectionDTO> connectionDTOs);

    void updateRegisteredTemplate(RegisteredTemplate template, boolean notifyClusterMembers);

    void updateProcessorIdNames(String templateName, Collection<ProcessorDTO> processors);

    void updateConnectionMap(String templateName, Collection<ConnectionDTO> connections);

    void subscribe(NiFiFlowCacheListener listener);


    void addConnectionToCache(ConnectionDTO connectionDTO);

}
