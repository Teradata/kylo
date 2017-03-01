package com.thinkbiganalytics.feedmgr.nifi;

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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.ReportingTaskDTO;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Uninterruptibles;
import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheConnectionData;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;
import com.thinkbiganalytics.metadata.rest.model.nifi.NifiFlowCacheSnapshot;
import com.thinkbiganalytics.nifi.provenance.NiFiProvenanceConstants;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.model.flow.NiFiFlowConnectionConverter;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowConnection;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;

/**
 * Cache processor definitions in a flow for use by the KyloProvenanceReportingTask
 *
 * Each Processor has an internal {@code flowId} generated why Kylo walks the flow This internal id is used to associate the Feed flow as a template with the Feed flow created when the feed is
 * saved/updated
 *
 * @see com.thinkbiganalytics.nifi.rest.visitor.NifiConnectionOrderVisitor
 */
public class NifiFlowCache implements NifiConnectionListener, PostMetadataConfigAction, NiFiProvenanceConstants {

    private static final Logger log = LoggerFactory.getLogger(NifiFlowCache.class);


    @Inject
    LegacyNifiRestClient nifiRestClient;
    @Inject
    MetadataService metadataService;
    @Inject
    FeedProvider feedProvider;
    @Inject
    MetadataAccess metadataAccess;
    @Inject
    PropertyExpressionResolver propertyExpressionResolver;
    @Inject
    private NifiConnectionService nifiConnectionService;
    private Map<String, String> feedNameToTemplateNameMap = new ConcurrentHashMap<>();

    private Map<String, Map<String, List<NifiFlowProcessor>>> feedFlowIdProcessorMap = new ConcurrentHashMap<>();

    private Map<String, Map<String, List<NifiFlowProcessor>>> feedProcessorIdProcessorMap = new ConcurrentHashMap<>();

    private Map<String, NifiFlowProcessor> processorIdMap = new ConcurrentHashMap<>();

    /**
     * Flag to mark if the cache is loaded or not This is used to determine if the cache is ready to be used
     */
    private boolean loaded = false;

    /**
     * Flag to indicate we are connected to NiFi
     */
    private boolean nifiConnected = false;

    /**
     * Flag to indicate Modeshape is available
     */
    private boolean modeShapeAvailable = false;

    private Map<String, String> processorIdToFeedProcessGroupId = new ConcurrentHashMap<>();

    private Map<String, String> processorIdToFeedNameMap = new ConcurrentHashMap<>();
    private Map<String, String> processorIdToProcessorName = new ConcurrentHashMap<>();
    private Map<String, NiFiFlowCacheConnectionData> connectionIdToConnectionMap = new ConcurrentHashMap<>();
    private Map<String, String> connectionIdCacheNameMap = new ConcurrentHashMap<>();

    /**
     * Set of the category.feed names for those that are just streaming feeds
     */
    private Set<String> streamingFeeds = new HashSet();

    /**
     * Set of the category.feed names
     */
    private Set<String> allFeeds = new HashSet<>();

    private Map<String, Long> feedLastUpated = new ConcurrentHashMap<>();

    /**
     * Map of the sync id to cache
     * This is the cache of the items out there that others have built and will check/update themseleves based upon the base maps in the object
     */
    private Map<String, NiFiFlowCacheSync> syncMap = new ConcurrentHashMap<>();


    /**
     * Map with the sync Id and the last time that item was sync'd with the system
     * This is used to expire the stale non used caches
     */
    private Map<String, DateTime> lastSyncTimeMap = new ConcurrentHashMap<>();

    private DateTime lastUpdated = null;

    @PostConstruct
    private void init() {
        nifiConnectionService.subscribeConnectionListener(this);
        initExpireTimerThread();
    }

    /**
     * Metadata is available
     */
    @Override
    public void run() {
        this.modeShapeAvailable = true;
        checkAndInitializeCache();
    }


    /**
     * NiFi has made a connection
     */
    @Override
    public void onNiFiConnected() {
        this.nifiConnected = true;
        checkAndInitializeCache();
    }

    @Override
    public void onNiFiDisconnected() {
        this.nifiConnected = false;
        //reset the flag to force cache initialization on nifi availability
        this.loaded = false;
    }


    /**
     * When modeshape and nifi are connected and ready attempt to initialize the cache
     */
    private void checkAndInitializeCache() {
        if (modeShapeAvailable && nifiConnected && !loaded) {
            rebuildCacheWithRetry();
        }
    }

    /**
     * rebuild a given cache resetting the cache with the given sync id to the latest data in the cache
     *
     * @param syncId a cache id
     * @return the latest cache
     */
    public NiFiFlowCacheSync refreshAll(String syncId) {
        NiFiFlowCacheSync sync = getSync(syncId);
        if (!sync.isUnavailable()) {
            sync.reset();
            return syncAndReturnUpdates(sync, false);
        } else {
            return NiFiFlowCacheSync.UNAVAILABLE;
        }
    }

    /**
     * Check to see if the cache is loaded
     *
     * @return {@code true} if the cache is populated, {@code false} if the cache is not populated
     */
    public boolean isAvailable() {
        return loaded;
    }

    /**
     * Return only the records that were updated since the last sync
     *
     * @param syncId a cache id
     * @return updates that have been applied to the cache.
     */
    public NiFiFlowCacheSync syncAndReturnUpdates(String syncId) {
        NiFiFlowCacheSync sync = getSync(syncId);
        if (!sync.isUnavailable()) {
            return syncAndReturnUpdates(sync);
        }
        return sync;
    }

    /**
     * Return the data in the cache for a given cache id
     *
     * @param syncId a cache id
     * @return the data in the cache for a given cache id
     */
    public NiFiFlowCacheSync getCache(String syncId) {
        NiFiFlowCacheSync sync = getSync(syncId);
        return sync;
    }

    /**
     * Preview any new updates that will be applied to a given cache
     *
     * @param syncId a cache id
     * @return any new updates that will be applied to a given cache
     */
    public NiFiFlowCacheSync previewUpdates(String syncId) {
        NiFiFlowCacheSync sync = getSync(syncId, true);
        if (!sync.isUnavailable()) {
            return previewUpdates(sync);
        }
        return sync;
    }


    /**
     * Rebuild the base cache that others will update from.
     */
    public synchronized void rebuildAll() {
        loaded = false;

        try {
            ensureNiFiKyloReportingTask();
        } catch (Exception e) {
            log.error("Exception while trying to ensure KyloReportingTask {}", e.getMessage(), e);
        }
        List<NifiFlowProcessGroup> allFlows = nifiRestClient.getFeedFlows();

        List<RegisteredTemplate> templates = null;
        clearAll();

        templates = metadataAccess.read(() -> metadataService.getRegisteredTemplates(), MetadataAccess.SERVICE);
        Map<String, RegisteredTemplate> feedTemplatesMap = new HashMap<>();

        //populate the template mappings and feeds to determine if the feed uses a streaming or batch template
        templates.stream().forEach(template -> populateTemplateMappingCache(template, feedTemplatesMap));

        allFlows.stream().forEach(nifiFlowProcessGroup -> {
            RegisteredTemplate template = feedTemplatesMap.get(nifiFlowProcessGroup.getFeedName());
            if (template != null) {
                updateFlow(nifiFlowProcessGroup.getFeedName(), template.isStream(), nifiFlowProcessGroup);
            } else {
                //this is possibly a reusable template.
                //update the processorid and connection name maps
                updateProcessorIdMaps(nifiFlowProcessGroup.getFeedName(), nifiFlowProcessGroup.getProcessorMap().values());
                this.connectionIdToConnectionMap.putAll(toConnectionIdMap(nifiFlowProcessGroup.getConnectionIdMap().values()));
            }
        });
        loaded = true;


    }

    /**
     * Rebuilds the cache.
     * If an exception occurs during the rebuild it will attempt to retry to build it up to 10 times before aborting
     */
    public void rebuildCacheWithRetry() {
        Exception lastError = null;
        int retries = 10;
        int waitTime = 5;
        for (int count = 1; count <= retries; ++count) {
            try {
                log.info("Attempting to build the NiFiFlowCache");
                rebuildAll();
                if (loaded) {
                    log.info("Successfully built the NiFiFlowCache");
                    break;
                }
            } catch (final Exception e) {
                log.error("Error attempting to build cache.  The system will attempt to retry {} more times.  Next attempt to rebuild in {} seconds.  The error was: {}. ", (retries - count), waitTime,
                          e.getMessage());
                lastError = e;
                Uninterruptibles.sleepUninterruptibly(waitTime, TimeUnit.SECONDS);
            }
        }
        if (!loaded) {
            log.error(
                "Unable to build the NiFi Flow Cache!  You will need to manually rebuild the cache using the following url:  http://KYLO_HOST:PORT/proxy/v1/metadata/nifi-provenance/nifi-flow-cache/reset-cache ",
                lastError);
        }
    }


    private NiFiFlowCacheSync previewUpdates(NiFiFlowCacheSync sync) {
        return syncAndReturnUpdates(sync, true);
    }

    private NiFiFlowCacheSync syncAndReturnUpdates(NiFiFlowCacheSync sync) {
        return syncAndReturnUpdates(sync, false);
    }

    private NiFiFlowCacheSync getSync(String syncId) {
        return getSync(syncId, false);
    }

    private NiFiFlowCacheSync getSync(String syncId, boolean forPreview) {
        if (isAvailable()) {
            NiFiFlowCacheSync sync = null;
            if (syncId == null || !syncMap.containsKey(syncId)) {
                sync = new NiFiFlowCacheSync();
                if (StringUtils.isNotBlank(syncId)) {
                    sync.setSyncId(syncId);
                }
                if (!forPreview) {
                    syncMap.put(sync.getSyncId(), sync);
                }
            } else {
                sync = syncMap.get(syncId);
            }
            return sync;
        } else {
            return NiFiFlowCacheSync.UNAVAILABLE;
        }
    }


    private NiFiFlowCacheSync syncAndReturnUpdates(NiFiFlowCacheSync sync, boolean preview) {
        if (!preview) {
            lastSyncTimeMap.put(sync.getSyncId(), DateTime.now());
        }
        if (sync.needsUpdate(lastUpdated)) {
            Map<String, String> processorIdToFeedNameMapCopy = ImmutableMap.copyOf(processorIdToFeedNameMap);
            Map<String, String> processorIdToFeedProcessGroupIdCopy = ImmutableMap.copyOf(processorIdToFeedProcessGroupId);
            Map<String, String> processorIdToProcessorNameCopy = ImmutableMap.copyOf(processorIdToProcessorName);
            Set<String> streamingFeedsCopy = ImmutableSet.copyOf(streamingFeeds);
            Set<String> allFeedsCopy = ImmutableSet.copyOf(allFeeds);
            Map<String, NiFiFlowCacheConnectionData> connectionDataMapCopy = ImmutableMap.copyOf(connectionIdToConnectionMap);

            //get feeds updated since last sync
            NifiFlowCacheSnapshot latest = new NifiFlowCacheSnapshot.Builder()
                .withProcessorIdToFeedNameMap(processorIdToFeedNameMapCopy)
                .withProcessorIdToFeedProcessGroupId(processorIdToFeedProcessGroupIdCopy)
                .withProcessorIdToProcessorName(processorIdToProcessorNameCopy)
                .withStreamingFeeds(streamingFeedsCopy)
                .withFeeds(allFeedsCopy)
                .withConnections(connectionDataMapCopy)
                .withSnapshotDate(lastUpdated).build();
            return syncAndReturnUpdates(sync, latest, preview);
        } else {
            return NiFiFlowCacheSync.EMPTY(sync.getSyncId());
        }
    }


    private NiFiFlowCacheSync syncAndReturnUpdates(NiFiFlowCacheSync sync, NifiFlowCacheSnapshot latest, boolean preview) {
        if (latest != null && sync.needsUpdate(latest.getSnapshotDate())) {

            NifiFlowCacheSnapshot updated = new NifiFlowCacheSnapshot.Builder()
                .withProcessorIdToFeedNameMap(sync.getProcessorIdToFeedNameMapUpdatedSinceLastSync(latest.getProcessorIdToFeedNameMap()))
                .withProcessorIdToFeedProcessGroupId(sync.getProcessorIdToProcessGroupIdUpdatedSinceLastSync(latest.getProcessorIdToFeedProcessGroupId()))
                .withProcessorIdToProcessorName(sync.getProcessorIdToProcessorNameUpdatedSinceLastSync(latest.getProcessorIdToProcessorName()))
                .withStreamingFeeds(latest.getAllStreamingFeeds())
                .withConnections(sync.getConnectionIdToConnectionUpdatedSinceLastSync(latest.getConnectionIdToConnectionName(), latest.getConnectionIdToConnection()))
                .withFeeds(sync.getFeedsUpdatedSinceLastSync(latest.getAllFeeds()))
                .build();
            //reset the pointers on this sync to be the latest
            if (!preview) {
                sync.setSnapshot(latest);
                sync.setLastSync(latest.getSnapshotDate());

            }
            NiFiFlowCacheSync updatedSync = new NiFiFlowCacheSync(sync.getSyncId(), updated);
            updatedSync.setUpdated(true);
            if (!preview) {
                updatedSync.setLastSync(latest.getSnapshotDate());
            }
            return updatedSync;

        }

        return NiFiFlowCacheSync.EMPTY(sync.getSyncId());
    }


    /**
     * clears the current cache
     ***/
    private void clearAll() {
        processorIdToFeedProcessGroupId.clear();
        processorIdToFeedProcessGroupId.clear();
        processorIdToProcessorName.clear();
        connectionIdToConnectionMap.clear();
        connectionIdCacheNameMap.clear();
        streamingFeeds.clear();
        allFeeds.clear();
        feedNameToTemplateNameMap.clear();
    }

    private void populateTemplateMappingCache(RegisteredTemplate template, Map<String, RegisteredTemplate> feedTemplatesMap) {

        template.getFeedNames().stream().forEach(feedName -> {
            if (feedTemplatesMap != null) {
                feedTemplatesMap.put(feedName, template);
            }
            feedNameToTemplateNameMap.put(feedName, template.getTemplateName());
            if (template.isStream()) {
                streamingFeeds.add(feedName);
            } else {
                streamingFeeds.remove(feedName);
            }
        });
    }


    /**
     * Ensure that there is a configured reporting task
     */
    private void ensureNiFiKyloReportingTask() {
        String reportingTaskName = StringUtils.substringAfterLast(NiFiKyloProvenanceEventReportingTaskType, ".");
        if (!nifiRestClient.getNiFiRestClient().reportingTasks().findFirstByType(NiFiKyloProvenanceEventReportingTaskType).isPresent()) {
            log.info("Attempting to create the {} in NiFi ", reportingTaskName);
            //create it
            //1 ensure the controller service exists and is wired correctly
            Optional<ControllerServiceDTO> controllerService = nifiRestClient.getNiFiRestClient().reportingTasks().findFirstControllerServiceByType(NiFiMetadataControllerServiceType);
            ControllerServiceDTO metadataService = null;
            if (controllerService.isPresent()) {
                metadataService = controllerService.get();
            } else {
                log.info("Attempting to create the Controller Service: {}  with the name {} in NiFi ", NiFiMetadataControllerServiceType, NiFiMetadataServiceName);
                //create it and enable it
                //first create it
                ControllerServiceDTO controllerServiceDTO = new ControllerServiceDTO();
                controllerServiceDTO.setType(NiFiMetadataControllerServiceType);
                controllerServiceDTO.setName(NiFiMetadataServiceName);
                metadataService = nifiRestClient.getNiFiRestClient().reportingTasks().createReportingTaskControllerService(controllerServiceDTO);
                //find the properties to inject
                Map<String, Object> configProperties = propertyExpressionResolver.getStaticConfigProperties();
                Map<String, String> stringConfigProperties = new HashMap<>();
                if (configProperties != null) {
                    //transform the object map to the String map
                    stringConfigProperties = configProperties.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() != null ? e.getValue().toString() : null));
                }
                metadataService = nifiRestClient.enableControllerServiceAndSetProperties(metadataService.getId(), stringConfigProperties);
            }

            if (metadataService != null) {

                try {
                    if (NifiProcessUtil.SERVICE_STATE.DISABLED.name().equalsIgnoreCase(metadataService.getState())) {
                        log.info("Reporting Task Controller Service {} exists, ensuring it is enabled.", NiFiMetadataServiceName);
                        //enable it....
                        metadataService = nifiRestClient.enableControllerServiceAndSetProperties(metadataService.getId(), null);
                    }
                } catch (NifiClientRuntimeException e) {
                    //swallow the exception and attempt to move on to create the task
                }

                log.info("Creating the Reporting Task {} ", reportingTaskName);
                ReportingTaskDTO reportingTaskDTO = new ReportingTaskDTO();
                reportingTaskDTO.setType(NiFiKyloProvenanceEventReportingTaskType);
                reportingTaskDTO = nifiRestClient.getNiFiRestClient().reportingTasks().createReportingTask(reportingTaskDTO);
                //now set the properties
                ReportingTaskDTO updatedReportingTask = new ReportingTaskDTO();
                updatedReportingTask.setType(NiFiKyloProvenanceEventReportingTaskType);
                updatedReportingTask.setId(reportingTaskDTO.getId());
                updatedReportingTask.setName(reportingTaskName);
                updatedReportingTask.setProperties(new HashMap<>(1));
                updatedReportingTask.getProperties().put("Metadata Service", metadataService.getId());
                updatedReportingTask.setSchedulingStrategy("TIMER_DRIVEN");
                updatedReportingTask.setSchedulingPeriod("5 secs");
                updatedReportingTask.setComments("Reporting task that will query the provenance repository and send the events and summary statistics over to Kylo via a JMS queue");
                updatedReportingTask.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
                //update it
                reportingTaskDTO = nifiRestClient.getNiFiRestClient().reportingTasks().update(updatedReportingTask);
                if (reportingTaskDTO != null) {
                    log.info("Successfully created the Reporting Task {} ", reportingTaskName);
                } else {
                    log.info("Error creating the Reporting Task {}.  You will need to go into NiFi to resolve. ", reportingTaskName);
                }
            }

        }
        ;

    }


    /**
     * Called after someone updates/Registers a template in the UI using the template stepper
     * This is used to update the feed marker for streaming/batch feeds
     */
    public synchronized void updateRegisteredTemplate(RegisteredTemplate template) {

        populateTemplateMappingCache(template, null);

        //update the processortype cachefeedNameToTemplateNameMap
        List<String>
            feedNames =
            feedNameToTemplateNameMap.entrySet().stream().filter(entry -> entry.getValue().equalsIgnoreCase(template.getTemplateName())).map(entry -> entry.getKey()).collect(Collectors.toList());

        log.info("Updated Template: {}, found {} associated feeds ", template.getTemplateName(), feedNames.size());
        if (template.isStream()) {
            streamingFeeds.addAll(feedNames);
        } else {
            streamingFeeds.removeAll(feedNames);
        }
        lastUpdated = DateTimeUtil.getNowUTCTime();

    }


    /**
     * add processors to the cache
     *
     * @param templateName a template name
     * @param processors   processors to add to the cache
     */
    public void updateProcessorIdNames(String templateName, Collection<ProcessorDTO> processors) {

        Map<String, String> processorIdToProcessorName = new HashMap<>();
        processors.stream().forEach(flowProcessor -> {
            processorIdToProcessorName.put(flowProcessor.getId(), flowProcessor.getName());
        });

        this.processorIdToProcessorName.putAll(processorIdToProcessorName);
    }

    /**
     * Add connections to the cache
     *
     * @param templateName a template name
     * @param connections  connections to add to the cache
     */
    public void updateConnectionMap(String templateName, Collection<ConnectionDTO> connections) {
        Map<String, NifiFlowConnection> connectionIdToConnectionMap = new HashMap<>();
        if (connections != null) {
            connections.stream().forEach(connectionDTO -> {
                NifiFlowConnection nifiFlowConnection = NiFiFlowConnectionConverter.toNiFiFlowConnection(connectionDTO);
                if (nifiFlowConnection != null) {
                    connectionIdToConnectionMap.put(nifiFlowConnection.getConnectionIdentifier(), nifiFlowConnection);
                }

            });
        }
        this.connectionIdToConnectionMap.putAll(toConnectionIdMap(connectionIdToConnectionMap.values()));
    }


    /**
     * Update cache for a feeds flow
     * Used by CreateFeed builder
     *
     * @param feed             a feed
     * @param feedProcessGroup the process group created with this feed
     */
    public void updateFlow(FeedMetadata feed, NifiFlowProcessGroup feedProcessGroup) {
        // feedProcessGroup.calculateCriticalPathProcessors();
        String feedName = feed.getCategoryAndFeedName();
        this.updateFlow(feedName, feed.getRegisteredTemplate().isStream(), feedProcessGroup.getId(), feedProcessGroup.getProcessorMap().values(), feedProcessGroup.getConnectionIdMap().values());
    }

    /**
     * Update  cache for a feed
     *
     * @param feedName         the name of the feed
     * @param isStream         {@code true} if its a streaming feed, {@code false} if its a batch feed
     * @param feedProcessGroup the process group created with this feed
     */
    public void updateFlow(String feedName, boolean isStream, NifiFlowProcessGroup feedProcessGroup) {
        //  feedProcessGroup.calculateCriticalPathProcessors();
        this.updateFlow(feedName, isStream, feedProcessGroup.getId(), feedProcessGroup.getProcessorMap().values(), feedProcessGroup.getConnectionIdMap().values());
    }


    private void updateFlow(String feedName, boolean isStream, String feedProcessGroupId, Collection<NifiFlowProcessor> processors, Collection<NifiFlowConnection> connections) {
        feedFlowIdProcessorMap.put(feedName, toFlowIdProcessorMap(processors));
        feedProcessorIdProcessorMap.put(feedName, toProcessorIdProcessorMap(processors));

        updateProcessorIdMaps(feedProcessGroupId, processors);
        Map<String, String> processorIdToProcessGroupId = new HashMap<>();
        Map<String, String> processorIdToProcessorName = new HashMap<>();
        processors.stream().forEach(flowProcessor -> {
            processorIdToProcessGroupId.put(flowProcessor.getId(), feedProcessGroupId);
            processorIdToProcessorName.put(flowProcessor.getId(), flowProcessor.getName());
        });
        this.processorIdToFeedProcessGroupId.putAll(processorIdToProcessGroupId);
        this.processorIdToProcessorName.putAll(processorIdToProcessorName);

        connectionIdToConnectionMap.putAll(toConnectionIdMap(connections));

        if (connections != null) {
            Map<String, String> connectionIdToNameMap = connections.stream().collect(Collectors.toMap(conn -> conn.getConnectionIdentifier(), conn -> conn.getName()));
            connectionIdCacheNameMap.putAll(connectionIdToNameMap);
        }

        processorIdMap.putAll(toProcessorIdMap(processors));
        processorIdToFeedNameMap.putAll(toProcessorIdFeedNameMap(processors, feedName));
        lastUpdated = DateTimeUtil.getNowUTCTime();

        if (isStream) {
            streamingFeeds.add(feedName);
        }
        allFeeds.add(feedName);
        feedLastUpated.put(feedName, lastUpdated.getMillis());

    }

    private void updateProcessorIdMaps(String processGroupId, Collection<NifiFlowProcessor> processors) {
        Map<String, String> processorIdToProcessGroupId = new HashMap<>();
        Map<String, String> processorIdToProcessorName = new HashMap<>();
        processors.stream().forEach(flowProcessor -> {
            processorIdToProcessGroupId.put(flowProcessor.getId(), processGroupId);
            processorIdToProcessorName.put(flowProcessor.getId(), flowProcessor.getName());
        });
        this.processorIdToFeedProcessGroupId.putAll(processorIdToProcessGroupId);
        this.processorIdToProcessorName.putAll(processorIdToProcessorName);

    }

    private Map<String, NiFiFlowCacheConnectionData> toConnectionIdMap(Collection<NifiFlowConnection> connections) {
        Map<String, NiFiFlowCacheConnectionData> connectionMap = new HashMap<>();
        connections.stream().forEach(conn -> {
            connectionMap
                .put(conn.getConnectionIdentifier(), new NiFiFlowCacheConnectionData(conn.getConnectionIdentifier(), conn.getName(), conn.getSourceIdentifier(), conn.getDestinationIdentifier()));
        });
        return connectionMap;
    }

    private Map<String, NifiFlowProcessor> toProcessorIdMap(Collection<NifiFlowProcessor> processors) {
        return processors.stream().collect(Collectors.toMap(NifiFlowProcessor::getId, Function.identity()));
    }

    private Map<String, String> toProcessorIdFeedNameMap(Collection<NifiFlowProcessor> processors, String feedName) {
        return processors.stream().collect(Collectors.toMap(NifiFlowProcessor::getId, name -> feedName));
    }


    private Map<String, List<NifiFlowProcessor>> toFlowIdProcessorMap(Collection<NifiFlowProcessor> processors) {
        if (processors != null && !processors.isEmpty()) {
            return processors.stream().filter(nifiFlowProcessor -> nifiFlowProcessor.getFlowId() != null).collect(Collectors.groupingBy(NifiFlowProcessor::getFlowId));
        }
        return Collections.emptyMap();
    }


    private Map<String, List<NifiFlowProcessor>> toProcessorIdProcessorMap(Collection<NifiFlowProcessor> processors) {
        if (processors != null && !processors.isEmpty()) {
            return processors.stream().collect(Collectors.groupingBy(NifiFlowProcessor::getId));
        }
        return new HashMap<>();
    }

    public CacheSummary cacheSummary() {
        return CacheSummary.build(syncMap);
    }

    private void initExpireTimerThread() {
        long timer = 30; // run ever 30 sec to check and expire
        ScheduledExecutorService service = Executors
            .newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            checkAndExpireUnusedCache();
        }, timer, timer, TimeUnit.SECONDS);


    }

    /**
     * Expire any cache entries that havent been touched in 60 minutes
     */
    public void checkAndExpireUnusedCache() {
        int minutes = 60;
        try {

            long expireAfter = minutes * 1000 * 60; //60 min
            Set<String> itemsRemoved = new HashSet<>();
            //find cache items that havent been synced in allotted time
            lastSyncTimeMap.entrySet().stream().filter(entry -> ((DateTime.now().getMillis() - entry.getValue().getMillis()) > expireAfter)).forEach(entry -> {
                syncMap.remove(entry.getKey());
                itemsRemoved.add(entry.getKey());
                log.info("Expiring Cache {}.  This cache has not been used in over {} minutes", entry.getKey(), minutes);
            });
            itemsRemoved.stream().forEach(item -> lastSyncTimeMap.remove(item));

        } catch (Exception e) {
            log.error("Error attempting to invalidate flow cache for items not touched in {} or more minutes", minutes, e);
        }
    }

    public static class CacheSummary {

        private Map<String, Integer> summary = new HashMap<>();
        private Integer cachedSyncIds;

        public CacheSummary() {

        }

        private CacheSummary(Map<String, Integer> cacheIds) {
            this.summary = cacheIds;
            this.cachedSyncIds = cacheIds.keySet().size();
        }

        public static CacheSummary build(Map<String, NiFiFlowCacheSync> syncMap) {
            Map<String, Integer>
                cacheIds =
                syncMap.entrySet().stream().collect(Collectors.toMap(stringNiFiFlowCacheSyncEntry -> stringNiFiFlowCacheSyncEntry.getKey(),
                                                                     stringNiFiFlowCacheSyncEntry1 -> stringNiFiFlowCacheSyncEntry1.getValue().getSnapshot().getProcessorIdToFeedNameMap().size()));
            return new CacheSummary(cacheIds);
        }

        public Map<String, Integer> getSummary() {
            return summary;
        }

        public void setSummary(Map<String, Integer> summary) {
            this.summary = summary;
        }

        public Integer getCachedSyncIds() {
            return cachedSyncIds;
        }

        public void setCachedSyncIds(Integer cachedSyncIds) {
            this.cachedSyncIds = cachedSyncIds;
        }
    }
}
