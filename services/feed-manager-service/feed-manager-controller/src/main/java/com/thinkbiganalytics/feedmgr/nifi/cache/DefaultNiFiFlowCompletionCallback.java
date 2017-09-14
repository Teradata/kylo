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

import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheConnectionData;
import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.rest.model.flow.NiFiFlowConnectionConverter;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 9/13/17.
 */
public class DefaultNiFiFlowCompletionCallback implements NiFiFlowInspectionCallback {

    private static final Logger log = LoggerFactory.getLogger(DefaultNiFiFlowCompletionCallback.class);

    private Map<String, String> processorIdToFeedProcessGroupId = new ConcurrentHashMap<>();
    private Map<String, String> processorIdToFeedNameMap = new ConcurrentHashMap<>();
    private Map<String, String> processorIdToProcessorName = new ConcurrentHashMap<>();
    private Map<String, NiFiFlowCacheConnectionData> connectionIdToConnectionMap = new ConcurrentHashMap<>();
    private Map<String, String> connectionIdCacheNameMap = new ConcurrentHashMap<>();
    private Set<String> reusableTemplateProcessorIds = new HashSet<>();
    private Set<ConnectionDTO> rootConnections = new HashSet<>();
    private String reusableTemplateProcessGroupId;
    private Set<String> feedNames = new HashSet<>();

    private String rootProcessGroupId;

    @Override
    public void execute(NiFiFlowInspectorManager nifiFlowInspectorManager) {
        NiFiFlowInspection root = nifiFlowInspectorManager.getFlowsInspected().values().stream().filter(f -> f.isRoot()).findFirst().orElse(null);
        if (root != null) {
            rootProcessGroupId = root.getProcessGroupId();
            this.rootConnections = root.getProcessGroupFlow().getFlow().getConnections().stream().map(e -> e.getComponent()).collect(Collectors.toSet());
            if ("root".equalsIgnoreCase(rootProcessGroupId) && !rootConnections.isEmpty()) {
                rootProcessGroupId = rootConnections.stream().findFirst().map(c -> c.getParentGroupId()).orElse("root");
            }
        }

        this.reusableTemplateProcessGroupId = nifiFlowInspectorManager.getFlowsInspected()
            .values().stream()
            .filter(f -> f.getLevel() == 2 && TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME.equalsIgnoreCase(f.getProcessGroupName())).findFirst()
            .map(f -> f.getProcessGroupId()).orElse(null);

        reusableTemplateProcessorIds = nifiFlowInspectorManager.getFlowsInspected()
            .values().stream()
            .filter(f -> f.getLevel() == 3 && TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME.equalsIgnoreCase(f.getParent().getProcessGroupName()))
            .flatMap(f -> f.getAllProcessors().stream())
            .map(p -> p.getId())
            .collect(Collectors.toSet());

        List<NiFiFlowInspection> feedProcessGroupInspections = nifiFlowInspectorManager.getFlowsInspected()
            .values().stream()
            .filter(f -> f.getLevel() == 3 && !TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME.equalsIgnoreCase(f.getParent().getProcessGroupName()))
            .collect(Collectors.toList());

        feedProcessGroupInspections.stream().forEach(f ->
                                                     {

                                                         String feedName = FeedNameUtil.fullName(f.getParent().getProcessGroupName(), f.getProcessGroupName());
                                                         feedNames.add(feedName);
                                                         // log.info("process feed {} ",feedName);
                                                         List<ProcessorDTO> feedChildren = f.getAllProcessors();
                                                         Map<String, String> feedNameMap = feedChildren.stream()
                                                             .collect(Collectors.toMap(x -> x.getId(), x -> feedName));
                                                         processorIdToFeedNameMap.putAll(feedNameMap);

                                                         Map<String, String> processGroupIdMap =
                                                             feedChildren.stream()
                                                                 .collect(Collectors.toMap(p -> p.getId(), p -> f.getProcessGroupId()));
                                                         processorIdToFeedProcessGroupId.putAll(processGroupIdMap);

                                                     });

        nifiFlowInspectorManager.getFlowsInspected().values().stream().forEach(inspection -> {

            Map<String, String>
                processorIdToNameMap =
                inspection.getProcessGroupFlow().getFlow().getProcessors().stream().map(e -> e.getComponent()).collect(Collectors.toMap(p -> p.getId(), p -> p.getName()));

            Map<String, String>
                connectionIdMap =
                inspection.getProcessGroupFlow().getFlow().getConnections().stream().map(e -> e.getComponent()).collect(Collectors.toMap(c -> c.getId(), c -> c.getName()));
            Map<String, NiFiFlowCacheConnectionData>
                connectionMap =
                inspection.getProcessGroupFlow().getFlow().getConnections().stream().map(e -> e.getComponent())
                    .map(c -> NiFiFlowConnectionConverter.toNiFiFlowConnection(c))
                    .collect(Collectors.toMap(c -> c.getConnectionIdentifier(),
                                              conn -> new NiFiFlowCacheConnectionData(conn.getConnectionIdentifier(), conn.getName(), conn.getSourceIdentifier(),
                                                                                      conn.getDestinationIdentifier())));

            connectionIdCacheNameMap.putAll(connectionIdMap);
            connectionIdToConnectionMap.putAll(connectionMap);
            processorIdToProcessorName.putAll(processorIdToNameMap);


        });


    }

    public Map<String, String> getProcessorIdToFeedProcessGroupId() {
        return processorIdToFeedProcessGroupId;
    }

    public Map<String, String> getProcessorIdToFeedNameMap() {
        return processorIdToFeedNameMap;
    }

    public Map<String, String> getProcessorIdToProcessorName() {
        return processorIdToProcessorName;
    }

    public Map<String, NiFiFlowCacheConnectionData> getConnectionIdToConnectionMap() {
        return connectionIdToConnectionMap;
    }

    public Map<String, String> getConnectionIdCacheNameMap() {
        return connectionIdCacheNameMap;
    }

    public Set<String> getReusableTemplateProcessorIds() {
        return reusableTemplateProcessorIds;
    }

    public String getReusableTemplateProcessGroupId() {
        return reusableTemplateProcessGroupId;
    }

    public Set<ConnectionDTO> getRootConnections() {
        return rootConnections;
    }

    public Set<String> getFeedNames() {
        return feedNames;
    }

    public String getRootProcessGroupId() {
        return rootProcessGroupId;
    }
}
