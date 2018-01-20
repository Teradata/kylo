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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import com.thinkbiganalytics.app.ServicesApplicationStartup;
import com.thinkbiganalytics.app.ServicesApplicationStartupListener;
import com.thinkbiganalytics.feedmgr.nifi.NifiConnectionListener;
import com.thinkbiganalytics.feedmgr.nifi.NifiConnectionService;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.app.KyloVersionProvider;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheConnectionData;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;
import com.thinkbiganalytics.metadata.rest.model.nifi.NifiFlowCacheSnapshot;
import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.provenance.NiFiProvenanceConstants;
import com.thinkbiganalytics.nifi.rest.NiFiObjectCache;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.flow.NiFiFlowConnectionConverter;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowConnection;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Cache Connections and Processors in NiFi
 */
public class NifiFlowCacheImpl implements ServicesApplicationStartupListener, NifiConnectionListener, NiFiProvenanceConstants, NifiFlowCache {

    private static final Logger log = LoggerFactory.getLogger(NifiFlowCacheImpl.class);

    public static final String ITEM_LAST_MODIFIED_KEY = "NIFI_FLOW_CACHE";


    @Inject
    LegacyNifiRestClient nifiRestClient;

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private NifiConnectionService nifiConnectionService;

    @Inject
    private KyloVersionProvider kyloVersionProvider;

    @Inject
    private NifiFlowCacheClusterManager nifiFlowCacheClusterManager;

    @Inject
    private NiFiObjectCache niFiObjectCache;

    @Inject
    ServicesApplicationStartup startup;

    @Value("${nifi.flow.inspector.threads:1}")
    private Integer nififlowInspectorThreads = 1;


    @Value("${nifi.flow.max.retries:100}")
    private Integer nifiFlowMaxRetries = 100;


    @Value("${nifi.flow.retry.wait.time.seconds:5}")
    private Integer nifiFlowWaitTime = 5;

    @Deprecated
    private Map<String, Map<String, List<NifiFlowProcessor>>> feedProcessorIdProcessorMap = new ConcurrentHashMap<>();

    @Deprecated
    private Map<String, NifiFlowProcessor> processorIdMap = new ConcurrentHashMap<>();

    private Set<String> reuseableTemplateProcessorIds = new HashSet<>();

    private String reusableTemplateProcessGroupId = null;

    private NifiFlowCacheSnapshot latest;

    private List<NiFiFlowCacheListener> listeners = new ArrayList<>();


    public void subscribe(NiFiFlowCacheListener listener) {
        this.listeners.add(listener);
    }

    private AtomicLong reloadCount = new AtomicLong(0);

    /**
     * Flag to mark if the cache is loaded or not This is used to determine if the cache is ready to be used
     */
    private boolean loaded = false;

    /**
     * Flag to indicate we are connected to NiFi
     */
    private boolean nifiConnected = false;

    private AtomicBoolean rebuildWithRetryInProgress = new AtomicBoolean(false);


    private Map<String, String> processorIdToFeedProcessGroupId = new ConcurrentHashMap<>();

    private Map<String, String> processorIdToFeedNameMap = new ConcurrentHashMap<>();
    private Map<String, String> processorIdToProcessorName = new ConcurrentHashMap<>();
    private Map<String, NiFiFlowCacheConnectionData> connectionIdToConnectionMap = new ConcurrentHashMap<>();
    private Map<String, String> connectionIdCacheNameMap = new ConcurrentHashMap<>();

    /**
     * Set of the category.feed names for those that are just streaming feeds
     */
    // private Set<String> streamingFeeds = new HashSet();

    /**
     * Set of the category.feed names
     */
    // private Set<String> allFeeds = new HashSet<>();

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
        startup.subscribe(this);
        initExpireTimerThread();
        initializeLatestSnapshot();
    }


    @Override
    public void onStartup(DateTime startTime) {
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
        notifyCacheUnavailable();
    }

    public boolean isConnectedToNiFi() {
        return this.nifiConnected;
    }

    /**
     * When Kylo is updated and nifi are connected and ready attempt to initialize the cache
     */
    private void checkAndInitializeCache() {
        boolean isLatest = metadataAccess.read(() -> {
            return kyloVersionProvider.isUpToDate();
        }, MetadataAccess.SERVICE);

        if (!loaded && rebuildWithRetryInProgress.get() == false) {
            log.info("Check and Initialize NiFi Flow Cache. Kylo up to date:{}, NiFi Connected:{}, Cache needs loading:{} ", isLatest, nifiConnected, !loaded);
            if (isLatest && nifiConnected && !loaded) {
                rebuildCacheWithRetry(nifiFlowMaxRetries, nifiFlowWaitTime);
            }
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
    @Override
    public boolean isAvailable() {
        return loaded;
    }


    /**
     * If kylo is clustered it needs to do an additional check to ensure the flow cache is synchronized across all kylo instances
     *
     * @return true if kylo is clustered, false if not.
     */
    @Override
    public boolean isKyloClustered() {
        return nifiFlowCacheClusterManager.isClustered();
    }


    /**
     * Return only the records that were updated since the last sync
     *
     * @param syncId a cache id
     * @return updates that have been applied to the cache.
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public boolean rebuildAll() {
        if (rebuildWithRetryInProgress.get() == false) {
            return rebuildCacheWithRetry(1, 5);
        }
        return false;
    }


    private void rebuildAllCache() {
        log.info("Rebuilding the NiFi Flow Cache. Starting NiFi Flow Inspection with {} threads ...", nififlowInspectorThreads);
        boolean notify = reloadCount.get() == 0;
        loaded = false;

        DefaultNiFiFlowCompletionCallback completionCallback = new DefaultNiFiFlowCompletionCallback();
        NiFiFlowInspectorManager flowInspectorManager = new NiFiFlowInspectorManager.NiFiFlowInspectorManagerBuilder(nifiRestClient.getNiFiRestClient())
            .startingProcessGroupId("root")
            .completionCallback(completionCallback)
            .threads(nififlowInspectorThreads)
            .waitUntilComplete(true)
            .buildAndInspect();

        connectionIdCacheNameMap.putAll(completionCallback.getConnectionIdCacheNameMap());
        connectionIdToConnectionMap.putAll(completionCallback.getConnectionIdToConnectionMap());
        processorIdToFeedProcessGroupId.putAll(completionCallback.getProcessorIdToFeedProcessGroupId());
        processorIdToFeedNameMap.putAll(completionCallback.getProcessorIdToFeedNameMap());
        processorIdToProcessorName.putAll(completionCallback.getProcessorIdToProcessorName());
        reuseableTemplateProcessorIds.addAll(completionCallback.getReusableTemplateProcessorIds());
        reusableTemplateProcessGroupId = completionCallback.getReusableTemplateProcessGroupId();

        if (!flowInspectorManager.hasErrors()) {
            log.info("NiFi Flow Inspection took {} ms with {} threads for {} feeds, {} processors and {} connections ", flowInspectorManager.getTotalTime(), flowInspectorManager.getThreadCount(),
                     completionCallback.getFeedNames().size(), processorIdToProcessorName.size(), connectionIdCacheNameMap.size());
            if (completionCallback.getRootConnections() != null) {
                log.info("Adding {} Root Connections to the niFiObjectCache ", completionCallback.getRootConnections().size());
                niFiObjectCache.addProcessGroupConnections(completionCallback.getRootConnections());
            }
            if (completionCallback.getReusableTemplateProcessGroupId() != null) {
                niFiObjectCache.setReusableTemplateProcessGroupId(completionCallback.getReusableTemplateProcessGroupId());
            }
            lastUpdated = DateTime.now();
            loaded = true;
            reloadCount.incrementAndGet();
            log.info("Successfully built NiFi Flow Cache");
            if (notify) {
                notifyCacheAvailable();
            }
        } else {
            throw new NiFiFlowCacheException("Error inspecting and building the NiFi flow cache.");
        }

    }

    private void notifyCacheAvailable() {
        this.listeners.stream().forEach(listener -> {
            try {
                listener.onCacheAvailable();
            } catch (Exception e) {
                log.error("Error processing listener onCacheAvailable {}", e.getMessage(), e);
            }
        });

    }

    private void notifyCacheUnavailable() {
        this.listeners.stream().forEach(listener -> {
            try {
                listener.onCacheUnavailable();
            } catch (Exception e) {
                log.error("Error processing listener onCacheUnavailable {}", e.getMessage(), e);
            }
        });
    }

    /**
     * Rebuilds the cache.
     * If an exception occurs during the rebuild it will attempt to retry to build it up to 10 times before aborting
     */
    public boolean rebuildCacheWithRetry(int retries, int waitTime) {
        boolean updated = false;
        if (rebuildWithRetryInProgress.compareAndSet(false, true)) {
            Exception lastError = null;

            for (int count = 1; count <= retries; ++count) {
                try {
                    log.info("Attempting to build the NiFiFlowCache");
                    rebuildAllCache();
                    if (loaded) {
                        log.info("Successfully built the NiFiFlowCache");
                        updated = true;
                        break;
                    }
                } catch (final Exception e) {
                    log.error("Error attempting to build cache.  The system will attempt to retry {} more times.  Next attempt to rebuild in {} seconds.  The error was: {}. ", (retries - count),
                              waitTime,
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
            rebuildWithRetryInProgress.set(false);
        }
        return updated;
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

    public boolean needsUpdateFromCluster() {
        return isKyloClustered() && nifiFlowCacheClusterManager.needsUpdate();
    }

    /**
     * if Kylo is clustered it needs to sync any updates from the other Kylo instances before proceeding
     */
    public synchronized void applyClusterUpdates() {
        List<NifiFlowCacheClusterUpdateMessage> updates = nifiFlowCacheClusterManager.findUpdates();
        Set<String> templateUpdates = new HashSet<>();
        boolean needsUpdates = !updates.isEmpty();
        if (needsUpdates) {
            log.info("Kylo Cluster Update: Detected changes.  About to apply {} updates ", updates.size());
        }
        updates.stream().forEach(update -> {
            switch (update.getType()) {
                case FEED:
                    NifiFlowCacheFeedUpdate feedUpdate = nifiFlowCacheClusterManager.getFeedUpdate(update.getMessage());
                    log.info("Kylo Cluster Update:  Applying Feed Change update for {}", feedUpdate.getFeedName());
                    updateFlow(feedUpdate);
                    break;
                case FEED2:
                    NifiFlowCacheFeedUpdate2 feedUpdate2 = nifiFlowCacheClusterManager.getFeedUpdate2(update.getMessage());
                    log.info("Kylo Cluster Update:  Applying Feed Change update for {}", feedUpdate2.getFeedName());
                    updateFlow(feedUpdate2);
                    break;
                case CONNECTION:
                    Collection<ConnectionDTO> connectionDTOS = nifiFlowCacheClusterManager.getConnectionsUpdate(update.getMessage());
                    log.info("Kylo Cluster Update:  Applying Connection list update");
                    updateConnectionMap(connectionDTOS, false);
                    if (connectionDTOS != null) {
                        connectionDTOS.stream().forEach(c -> {
                            niFiObjectCache.addConnection(c.getParentGroupId(), c);
                        });
                    }
                    break;
                case PROCESSOR:
                    Collection<ProcessorDTO> processorDTOS = nifiFlowCacheClusterManager.getProcessorsUpdate(update.getMessage());
                    log.info("Kylo Cluster Update:  Applying Processor list update");
                    updateProcessorIdNames(processorDTOS, false);
                    break;
                case TEMPLATE:
                    if (!templateUpdates.contains(update.getMessage())) {
                        RegisteredTemplate template = nifiFlowCacheClusterManager.getTemplate(update.getMessage());
                        log.info("Kylo Cluster Update:  Applying Template update for {} ", template.getTemplateName());
                        updateRegisteredTemplate(template, false);
                        templateUpdates.add(update.getMessage());
                    }
                    break;
                default:
                    break;
            }
        });

        if (needsUpdates) {
            nifiFlowCacheClusterManager.appliedUpdates(updates);
            lastUpdated = DateTime.now();
            log.info("Kylo Cluster Update: NiFi Flow File Cache is in sync. All {} updates have been applied to the cache. ", updates.size());
        }

    }

    public NifiFlowCacheSnapshot getLatest() {
     return latest;
    }

    private void initializeLatestSnapshot() {
        latest =
            new NifiFlowCacheSnapshot(processorIdToFeedNameMap, processorIdToFeedProcessGroupId, processorIdToProcessorName, null, null);
        latest.setConnectionIdToConnection(connectionIdToConnectionMap);
        latest.setConnectionIdToConnectionName(connectionIdCacheNameMap);
        latest.setReusableTemplateProcessorIds(reuseableTemplateProcessorIds);

    }


    private NiFiFlowCacheSync syncAndReturnUpdates(NiFiFlowCacheSync sync, boolean preview) {
        if (!preview) {
            lastSyncTimeMap.put(sync.getSyncId(), DateTime.now());
        }
        if (isKyloClustered()) {
            applyClusterUpdates();
        }

        if (sync.needsUpdate(lastUpdated)) {
            Map<String, String> processorIdToFeedNameMapCopy = ImmutableMap.copyOf(processorIdToFeedNameMap);
            Map<String, String> processorIdToFeedProcessGroupIdCopy = ImmutableMap.copyOf(processorIdToFeedProcessGroupId);
            Map<String, String> processorIdToProcessorNameCopy = ImmutableMap.copyOf(processorIdToProcessorName);
            Map<String, NiFiFlowCacheConnectionData> connectionDataMapCopy = ImmutableMap.copyOf(connectionIdToConnectionMap);

            //get feeds updated since last sync
            NifiFlowCacheSnapshot latest = new NifiFlowCacheSnapshot.Builder()
                .withProcessorIdToFeedNameMap(processorIdToFeedNameMapCopy)
                .withProcessorIdToFeedProcessGroupId(processorIdToFeedProcessGroupIdCopy)
                .withProcessorIdToProcessorName(processorIdToProcessorNameCopy)
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
                .withConnections(sync.getConnectionIdToConnectionUpdatedSinceLastSync(latest.getConnectionIdToConnectionName(), latest.getConnectionIdToConnection()))
                .withReusableTemplateProcessorIds(latest.getReusableTemplateProcessorIds())
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
        reuseableTemplateProcessorIds.clear();
    }


    /**
     * Called after someone updates/Registers a template in the UI using the template stepper
     * This is used to update the feed marker for streaming/batch feeds
     */
    public synchronized void updateRegisteredTemplate(RegisteredTemplate template, boolean notifyClusterMembers) {

        if (notifyClusterMembers) {
            //mark the persistent table that this was updated
            if (nifiFlowCacheClusterManager.isClustered()) {
                nifiFlowCacheClusterManager.updateTemplate(template.getTemplateName());
            }
            lastUpdated = DateTime.now();
        }

    }

    /**
     * Update the cache of processorIds and connections when a reusable template is updated
     *
     * @param templateName    the name of the template
     * @param processGroupDTO the process group that stores the flow of the reusable template
     */
    public void updateCacheForReusableTemplate(String templateName, ProcessGroupDTO processGroupDTO) {
        Collection<ProcessorDTO> processors = NifiProcessUtil.getProcessors(processGroupDTO);
        updateProcessorIdNames(templateName, processors);
        Set<ConnectionDTO> connections = NifiConnectionUtil.getAllConnections(processGroupDTO);
        updateConnectionMap(templateName, connections);
        processGroupDTO.getContents().getProcessors().stream().forEach(processorDTO -> reuseableTemplateProcessorIds.add(processorDTO.getId()));
        lastUpdated = DateTime.now();
    }


    /**
     * add processors to the cache
     *
     * @param templateName a template name
     * @param processors   processors to add to the cache
     */
    public void updateProcessorIdNames(String templateName, Collection<ProcessorDTO> processors) {
        updateProcessorIdNames(processors, true);
    }

    private void updateProcessorIdNames(Collection<ProcessorDTO> processors, boolean notifyClusterMembers) {

        Map<String, String> processorIdToProcessorName = new HashMap<>();
        processors.stream().forEach(flowProcessor -> {
            processorIdToProcessorName.put(flowProcessor.getId(), flowProcessor.getName());
        });

        this.processorIdToProcessorName.putAll(processorIdToProcessorName);

        if (notifyClusterMembers) {
            if (nifiFlowCacheClusterManager.isClustered()) {
                nifiFlowCacheClusterManager.updateProcessors(processors);
            }
            lastUpdated = DateTime.now();
        }
    }

    /**
     * Add connections to the cache
     *
     * @param templateName a template name
     * @param connections  connections to add to the cache
     */
    public void updateConnectionMap(String templateName, Collection<ConnectionDTO> connections) {
        updateConnectionMap(connections, true);
    }

    private void updateConnectionMap(Collection<ConnectionDTO> connections, boolean notifyClusterMembers) {
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

        if (connections != null) {
            Map<String, String> connectionIdToNameMap = connections.stream().collect(Collectors.toMap(conn -> conn.getId(), conn -> conn.getName()));
            connectionIdCacheNameMap.putAll(connectionIdToNameMap);
        }

        if (notifyClusterMembers) {
            if (nifiFlowCacheClusterManager.isClustered()) {
                nifiFlowCacheClusterManager.updateConnections(connections);
            }
            lastUpdated = DateTime.now();
        }
    }


    /**
     * Update cache for a feeds flow
     * Used by CreateFeed builder
     *
     * @param feed             a feed
     * @param feedProcessGroup the process group created with this feed
     */
    public void updateFlow(FeedMetadata feed, NifiFlowProcessGroup feedProcessGroup) {
        String feedName = feed.getCategoryAndFeedName();
        this.updateFlow(feedName, feed.getRegisteredTemplate().isStream(), feedProcessGroup.getId(), feedProcessGroup.getProcessorMap().values(), feedProcessGroup.getConnectionIdMap().values(), true);
    }

    public void updateFlowForFeed(FeedMetadata feed, String feedProcessGroupId, Collection<ProcessorDTO> processorDTOs, Collection<ConnectionDTO> connectionDTOs) {
        String feedName = feed.getCategoryAndFeedName();
        this.updateFlowForFeed(feedName, feed.getRegisteredTemplate().isStream(), feedProcessGroupId, processorDTOs, connectionDTOs, true);
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
        this.updateFlow(feedName, isStream, feedProcessGroup.getId(), feedProcessGroup.getProcessorMap().values(), feedProcessGroup.getConnectionIdMap().values(), true);
    }

    /**
     * update for clustered kylo
     */
    public void updateFlow(NifiFlowCacheFeedUpdate2 flowCacheFeedUpdate) {
        updateFlowForFeed(flowCacheFeedUpdate.getFeedName(), flowCacheFeedUpdate.isStream(), flowCacheFeedUpdate.getFeedProcessGroupId(), flowCacheFeedUpdate.getProcessors(),
                          flowCacheFeedUpdate.getConnections(), false);
    }


    /**
     * update for clustered kylo
     */
    public void updateFlow(NifiFlowCacheFeedUpdate flowCacheFeedUpdate) {
        updateFlow(flowCacheFeedUpdate.getFeedName(), flowCacheFeedUpdate.isStream(), flowCacheFeedUpdate.getFeedProcessGroupId(), flowCacheFeedUpdate.getProcessors(),
                   flowCacheFeedUpdate.getConnections(), false);
    }

    private void updateFlowForFeed(String feedName, boolean isStream, String feedProcessGroupId, Collection<ProcessorDTO> processors, Collection<ConnectionDTO> connections,
                                   boolean notifyClusterMembers) {
        Map<String, String> processorIdToProcessorName = processors.stream().collect(Collectors.toMap(p -> p.getId(), p -> p.getName()));
        Map<String, String> processorIdToFeedProcessGroupId = processors.stream().collect(Collectors.toMap(p -> p.getId(), p -> feedProcessGroupId));
        Map<String, String> processorIdToFeedName = processors.stream().collect(Collectors.toMap(p -> p.getId(), p -> feedName));
        this.processorIdToFeedProcessGroupId.putAll(processorIdToFeedProcessGroupId);
        this.processorIdToProcessorName.putAll(processorIdToProcessorName);
        processorIdToFeedNameMap.putAll(processorIdToFeedName);

        updateConnectionMap(connections, false);

        //notify others of the cache update only if we are not doing a full refresh
        if (loaded && notifyClusterMembers) {
            if (nifiFlowCacheClusterManager.isClustered()) {
                nifiFlowCacheClusterManager.updateFeed2(feedName, isStream, feedProcessGroupId, processors, connections);
            }
            lastUpdated = DateTime.now();
        }


    }

    /**
     * updateFlowForFeed is now being used
     */
    @Deprecated
    private void updateFlow(String feedName, boolean isStream, String feedProcessGroupId, Collection<NifiFlowProcessor> processors, Collection<NifiFlowConnection> connections,
                            boolean notifyClusterMembers) {

        feedProcessorIdProcessorMap.put(feedName, toProcessorIdProcessorMap(processors));

        updateProcessorIdMaps(feedProcessGroupId, processors);

        connectionIdToConnectionMap.putAll(toConnectionIdMap(connections));

        if (connections != null) {
            Map<String, String> connectionIdToNameMap = connections.stream().collect(Collectors.toMap(conn -> conn.getConnectionIdentifier(), conn -> conn.getName()));
            connectionIdCacheNameMap.putAll(connectionIdToNameMap);
        }

        processorIdMap.putAll(toProcessorIdMap(processors));
        processorIdToFeedNameMap.putAll(toProcessorIdFeedNameMap(processors, feedName));

        //notify others of the cache update only if we are not doing a full refresh
        if (loaded && notifyClusterMembers) {
            if (nifiFlowCacheClusterManager.isClustered()) {
                nifiFlowCacheClusterManager.updateFeed(feedName, isStream, feedProcessGroupId, processors, connections);
            }
            lastUpdated = DateTime.now();
        }


    }

    private void updateProcessorIdMaps(String processGroupId, Collection<NifiFlowProcessor> processors) {
        Map<String, String> processorIdToProcessGroupId = new HashMap<>();
        Map<String, String> processorIdToProcessorName = new HashMap<>();
        processors.stream().forEach(flowProcessor -> {
            processorIdToProcessGroupId.put(flowProcessor.getId(), processGroupId);
            processorIdToProcessorName.put(flowProcessor.getId(), flowProcessor.getName());

            if (flowProcessor.getProcessGroup() != null && flowProcessor.getProcessGroup().getParentGroupName() != null && TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME
                .equalsIgnoreCase(flowProcessor.getProcessGroup().getParentGroupName())) {
                reuseableTemplateProcessorIds.add(flowProcessor.getId());
                if (reusableTemplateProcessGroupId == null) {
                    reusableTemplateProcessGroupId = flowProcessor.getProcessGroup().getId();
                }
            }
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

    private Map<String, NiFiFlowCacheConnectionData> connectionDTOtoConnectionIdMap(Collection<ConnectionDTO> connections) {
        Map<String, NiFiFlowCacheConnectionData> connectionMap = new HashMap<>();
        connections.stream().forEach(conn -> {
            connectionMap
                .put(conn.getId(), new NiFiFlowCacheConnectionData(conn.getId(), conn.getName(), conn.getSource() != null ? conn.getSource().getId() : null,
                                                                   conn.getDestination() != null ? conn.getDestination().getId() : null));
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

    public void addConnectionToCache(ConnectionDTO connectionDTO) {
        Collection<ConnectionDTO> connectionList = Lists.newArrayList(connectionDTO);
        updateConnectionMap(connectionList, true);
    }


}
