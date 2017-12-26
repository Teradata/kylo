package com.thinkbiganalytics.feedmgr.nifi.cache;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.cluster.ClusterServiceListener;
import com.thinkbiganalytics.cluster.NiFiFlowCacheUpdateType;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.cluster.NiFiFlowCacheClusterUpdateItem;
import com.thinkbiganalytics.metadata.jpa.cluster.NiFiFlowCacheClusterUpdateProvider;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowConnection;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Manage the Nifi flow cache when Kylo is clustered
 */
public class NifiFlowCacheClusterManager implements ClusterServiceListener {

    private static final String LAST_MODIFIED_KEY_PREFIX = "NIFI_FLOW_CACHE";

    private static final Logger log = LoggerFactory.getLogger(NifiFlowCacheClusterManager.class);


    @Inject
    MetadataAccess metadataAccess;

    @Inject
    ClusterService clusterService;

    @Inject
    MetadataService metadataService;


    @Inject
    NiFiFlowCacheClusterUpdateProvider niFiFlowCacheProvider;

    @PostConstruct
    public void init() {
        clusterService.subscribe(this);
    }

    public NifiFlowCacheClusterUpdateMessage updateTemplate(String templateName) {
        NifiFlowCacheClusterUpdateMessage updateMessage = new NifiFlowCacheClusterUpdateMessage(NiFiFlowCacheUpdateType.TEMPLATE, templateName);
        updatedCache(updateMessage);
        return updateMessage;
    }


    public NifiFlowCacheClusterUpdateMessage updateConnections(Collection<ConnectionDTO> connections) {
        String json = ObjectMapperSerializer.serialize(connections);
        NifiFlowCacheClusterUpdateMessage updateMessage = new NifiFlowCacheClusterUpdateMessage(NiFiFlowCacheUpdateType.CONNECTION, json);
        updatedCache(updateMessage);
        return updateMessage;
    }

    public NifiFlowCacheClusterUpdateMessage updateProcessors(Collection<ProcessorDTO> processors) {
        //strip for serialization ... create new NifiFlowCacheSimpleProcessorDTO
        List<NifiFlowCacheSimpleProcessorDTO> processorsToCache = processors.stream()
            .map(p -> new NifiFlowCacheSimpleProcessorDTO(p.getId(), p.getName(), p.getType(), p.getParentGroupId()))
            .collect(Collectors.toList());
        String json = ObjectMapperSerializer.serialize(processorsToCache);
        NifiFlowCacheClusterUpdateMessage updateMessage = new NifiFlowCacheClusterUpdateMessage(NiFiFlowCacheUpdateType.PROCESSOR, json);
        updatedCache(updateMessage);
        return updateMessage;
    }

    public NifiFlowCacheClusterUpdateMessage updateFeed(String feedName, boolean isStream, NifiFlowProcessGroup feedFlow) {

        NifiFlowCacheFeedUpdate feedUpdate = new NifiFlowCacheFeedUpdate(feedName, isStream, feedFlow.getId(), feedFlow.getProcessorMap().values(), feedFlow.getConnectionIdMap().values());
        String json = ObjectMapperSerializer.serialize(feedUpdate);
        NifiFlowCacheClusterUpdateMessage updateMessage = new NifiFlowCacheClusterUpdateMessage(NiFiFlowCacheUpdateType.FEED, json);
        updatedCache(updateMessage);
        return updateMessage;
    }

    public NifiFlowCacheClusterUpdateMessage updateFeed(String feedName, boolean isStream, String feedProcessGroupId, Collection<NifiFlowProcessor> processors,
                                                        Collection<NifiFlowConnection> connections) {
        NifiFlowCacheSimpleFeedUpdate feedUpdate = new NifiFlowCacheSimpleFeedUpdate(feedName, isStream, feedProcessGroupId, transformNifiFlowProcesors(processors), connections);
        String json = ObjectMapperSerializer.serialize(feedUpdate);
        NifiFlowCacheClusterUpdateMessage updateMessage = new NifiFlowCacheClusterUpdateMessage(NiFiFlowCacheUpdateType.FEED, json);
        updatedCache(updateMessage);
        return updateMessage;
    }

    /**
     * This replaces the updateFeed() callback
     * starting with 0.8.3.1 cluster manager will callback using this method
     */
    public NifiFlowCacheClusterUpdateMessage updateFeed2(String feedName, boolean isStream, String feedProcessGroupId, Collection<ProcessorDTO> processors, Collection<ConnectionDTO> connections) {
        NifiFlowCacheSimpleFeedUpdate feedUpdate = new NifiFlowCacheSimpleFeedUpdate(feedName, isStream, feedProcessGroupId, transformProcessors(processors), transformConnections(connections));
        String json = ObjectMapperSerializer.serialize(feedUpdate);
        NifiFlowCacheClusterUpdateMessage updateMessage = new NifiFlowCacheClusterUpdateMessage(NiFiFlowCacheUpdateType.FEED2, json);
        updatedCache(updateMessage);
        return updateMessage;
    }


    public NifiFlowCacheFeedUpdate getFeedUpdate(String json) {
        NifiFlowCacheFeedUpdate update = ObjectMapperSerializer.deserialize(json, NifiFlowCacheFeedUpdate.class);
        return update;
    }

    public NifiFlowCacheFeedUpdate2 getFeedUpdate2(String json) {
        NifiFlowCacheFeedUpdate2 update = ObjectMapperSerializer.deserialize(json, NifiFlowCacheFeedUpdate2.class);
        return update;
    }


    public Collection<ProcessorDTO> getProcessorsUpdate(String json) {
        Set<ProcessorDTO> processors = ObjectMapperSerializer.deserialize(json, new TypeReference<Set<ProcessorDTO>>() {
        });
        return processors;
    }

    public Collection<ConnectionDTO> getConnectionsUpdate(String json) {
        Set<ConnectionDTO> connections = ObjectMapperSerializer.deserialize(json, new TypeReference<Set<ConnectionDTO>>() {
        });
        return connections;
    }

    public RegisteredTemplate getTemplate(String templateName) {
        return metadataService.findRegisteredTemplateByName(templateName);
    }

    public boolean isClustered() {
        return clusterService.isClustered();
    }

    private void updatedCache(NifiFlowCacheClusterUpdateMessage update) {
        metadataAccess.commit(() -> {
            niFiFlowCacheProvider.updatedCache(update.getType(), update.getMessage());
        }, MetadataAccess.SERVICE);
        //send it off to notify others its been updated?
    }


    public boolean needsUpdate() {
        return metadataAccess.commit(() -> {
            return niFiFlowCacheProvider.needsUpdate();
        }, MetadataAccess.SERVICE);
    }

    public List<NifiFlowCacheClusterUpdateMessage> findUpdates() {
        return metadataAccess.commit(() -> {
            List<NiFiFlowCacheClusterUpdateItem> updates = niFiFlowCacheProvider.findUpdates();
            return transformUpdates(updates);
        }, MetadataAccess.SERVICE);
    }


    public void appliedUpdates(List<NifiFlowCacheClusterUpdateMessage> updateMessages) {
        metadataAccess.commit(() -> {
            List<String> updateKeys = updateMessages.stream().map(m -> m.getUpdateKey()).collect(Collectors.toList());
            niFiFlowCacheProvider.appliedUpdates(updateKeys);
        }, MetadataAccess.SERVICE);
    }

    private List<NifiFlowCacheClusterUpdateMessage> transformUpdates(List<NiFiFlowCacheClusterUpdateItem> updates) {
        if (updates != null) {
            return updates.stream().map(update -> new NifiFlowCacheClusterUpdateMessage(update.getUpdateType(), update.getUpdateValue(), update.getUpdateKey())).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private Collection<NifiFlowCacheClusterNifiFlowProcessor> transformNifiFlowProcesors(Collection<NifiFlowProcessor> processors) {
        if (processors != null) {
            return processors.stream().map(p -> new NifiFlowCacheClusterNifiFlowProcessor(p.getId(), p.getName(), p.getType(), p.getFlowId())).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }


    private Collection<NifiFlowCacheClusterNifiFlowProcessor> transformProcessors(Collection<ProcessorDTO> processors) {
        if (processors != null) {
            return processors.stream().map(p -> new NifiFlowCacheClusterNifiFlowProcessor(p.getId(), p.getName(), p.getType(), null)).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private Collection<NifiFlowConnection> transformConnections(Collection<ConnectionDTO> connections) {
        if (connections != null) {
            return connections.stream()
                .map(c -> new NifiFlowConnection(c.getId(), c.getName(), c.getSource() != null ? c.getSource().getId() : null, c.getDestination() != null ? c.getDestination().getId() : null))
                .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }


    @Override
    public void onClusterMembershipChanged(List<String> previousMembers, List<String> currentMembers) {

    }

    @Override
    public void onConnected(List<String> currentMembers) {
        log.info("Kylo Cluster Node connected {} members exist.  {} ", currentMembers.size(), currentMembers);
        //on connected reset the previous db entries
        if (currentMembers.size() == 1) {
            try {
                metadataAccess.commit(() -> {
                    log.info("This is the First Member connecting to the cluster.  Resetting the previous Cluster cache updates  {} members exist.  {} ", currentMembers.size(), currentMembers);
                    niFiFlowCacheProvider.resetClusterSyncUpdates();
                }, MetadataAccess.SERVICE);
            } catch (Exception e) {
                //log the error and carry on
                log.error("Error attempting to reset the NiFi Flow Cache in the database when starting the Kylo Cluster. {} ", e.getMessage(), e);
            }
        }

    }

    @Override
    public void onDisconnected(List<String> currentMembers) {

    }

    @Override
    public void onClosed(List<String> currentMembers) {

    }
}
