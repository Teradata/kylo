package com.thinkbiganalytics.feedmgr.nifi;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.common.constants.KyloProcessorFlowType;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.service.MetadataService;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.rest.model.nifi.NiFiFlowCacheSync;
import com.thinkbiganalytics.metadata.rest.model.nifi.NifiFlowCacheSnapshot;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by sr186054 on 12/20/16. Cache processor definitions in a flow for use by the KyloProvenanceReportingTask
 *
 * Each Processor has an internal {@code flowId} generated why Kylo walks the flow This internal id is used to associate the Feed flow as a template with the Feed flow created when the feed is
 * saved/updated
 *
 * @see com.thinkbiganalytics.nifi.rest.visitor.NifiConnectionOrderVisitor
 */
public class NifiFlowCache implements NifiConnectionListener {

    @Inject
    LegacyNifiRestClient nifiRestClient;

    @Inject
    private NifiConnectionService nifiConnectionService;

    @Inject
    MetadataService metadataService;

    @Inject
    MetadataAccess metadataAccess;


    private Map<String, Map<String, List<NifiFlowProcessor>>> feedFlowIdProcessorMap = new ConcurrentHashMap<>();

    private Map<String, Map<String, List<NifiFlowProcessor>>> feedProcessorIdProcessorMap = new ConcurrentHashMap<>();

    private Map<String, NifiFlowProcessor> processorIdMap = new ConcurrentHashMap<>();


    private Map<String, KyloProcessorFlowType> processorIdToFlowTypeMap = new ConcurrentHashMap<>();


    /**
     * feedName, failure processorId
     */
    private Map<String, Set<String>> feedFailureProcessorIdMap = new ConcurrentHashMap<>();

    /**
     * Flag to mark if the cache is loaded or not This is used to determine if the cache is ready to be used
     */
    private boolean loaded = false;

    private Map<String, String> processorIdToFeedProcessGroupId = new ConcurrentHashMap<>();

    private Map<String, String> processorIdToFeedNameMap = new ConcurrentHashMap<>();
    private Map<String, String> processorIdToProcessorName = new ConcurrentHashMap<>();
    private Set<String> streamingFeeds = new HashSet();

    private Map<String, Long> feedLastUpated = new ConcurrentHashMap<>();

    private Map<String, NiFiFlowCacheSync> syncMap = new ConcurrentHashMap<>();

    private DateTime lastUpdated = null;


    @Override
    public void onConnected() {
        if (!loaded) {
            rebuildAll();
        }
    }

    @Override
    public void onDisconnected() {

    }

    @PostConstruct
    private void init() {
        nifiConnectionService.subscribeConnectionListener(this);
    }

    public NiFiFlowCacheSync refreshAll(String syncId) {
        NiFiFlowCacheSync sync = getSync(syncId);
        if (!sync.isUnavailable()) {
            sync.reset();
            return syncAndReturnUpdates(sync, false);
        } else {
            return NiFiFlowCacheSync.UNAVAILABLE;
        }
    }

    public boolean isAvailable() {
        return loaded;
    }

    /**
     * Return only the records that were updated since the last sync
     */
    public NiFiFlowCacheSync syncAndReturnUpdates(String syncId) {
        NiFiFlowCacheSync sync = getSync(syncId);
        if (!sync.isUnavailable()) {
            return syncAndReturnUpdates(sync);
        }
        return sync;
    }


    public NiFiFlowCacheSync previewUpdates(String syncId) {
        NiFiFlowCacheSync sync = getSync(syncId, true);
        if (!sync.isUnavailable()) {
            return previewUpdates(sync);
        }
        return sync;
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
        if (sync.needsUpdate(lastUpdated)) {
            Map<String, String> processorIdToFeedNameMapCopy = ImmutableMap.copyOf(processorIdToFeedNameMap);
            Map<String, String> processorIdToFeedProcessGroupIdCopy = ImmutableMap.copyOf(processorIdToFeedProcessGroupId);
            Map<String, String> processorIdToProcessorNameCopy = ImmutableMap.copyOf(processorIdToProcessorName);
            Set<String> streamingFeedsCopy = ImmutableSet.copyOf(streamingFeeds);
            Map<String, KyloProcessorFlowType> processorIdToFlowTypeMapCopy = ImmutableMap.copyOf(processorIdToFlowTypeMap);

            //get feeds updated since last sync
            NifiFlowCacheSnapshot latest = new NifiFlowCacheSnapshot.Builder()
                .withProcessorIdToFeedNameMap(processorIdToFeedNameMapCopy)
                .withProcessorIdToFeedProcessGroupId(processorIdToFeedProcessGroupIdCopy)
                .withProcessorIdToProcessorName(processorIdToProcessorNameCopy)
                .withStreamingFeeds(streamingFeedsCopy)
                .withProcessorIdToFlowTypeMap(processorIdToFlowTypeMapCopy)
                .withSnapshotDate(lastUpdated).build();
            return syncAndReturnUpdates(sync, latest, preview);
        } else {
            return NiFiFlowCacheSync.EMPTY(sync.getSyncId());
        }
    }


    private NiFiFlowCacheSync syncAndReturnUpdates(NiFiFlowCacheSync sync, NifiFlowCacheSnapshot latest, boolean preview) {
        if (latest != null && sync.needsUpdate(latest.getSnapshotDate())) {

            NifiFlowCacheSnapshot updated = new NifiFlowCacheSnapshot.Builder()
                .withProcessorIdToFeedNameMap(sync.getProcessorIdToFeedNameMapUpdatedSinceLastSync(latest.getAddProcessorIdToFeedNameMap()))
                .withProcessorIdToFeedProcessGroupId(sync.getProcessorIdToProcessGroupIdUpdatedSinceLastSync(latest.getAddProcessorIdToFeedProcessGroupId()))
                .withProcessorIdToProcessorName(sync.getProcessorIdToProcessorNameUpdatedSinceLastSync(latest.getAddProcessorIdToProcessorName()))
                .withProcessorIdToFlowTypeMap(sync.getProcessorToProcessorFlowTypeMapUpdatedSinceLastSync(latest.getProcessorIdToProcessorFlowTypeMap()))
                .withStreamingFeeds(sync.getStreamingFeedsUpdatedSinceLastSync(latest.getAddStreamingFeeds())).build();
            //reset the pointers on this sync to be the latest
            if (!preview) {
                sync.setSnapshot(latest);
                sync.setLastSync(latest.getSnapshotDate());
            }
            return new NiFiFlowCacheSync(sync.getSyncId(), updated);
        }

        return NiFiFlowCacheSync.EMPTY(sync.getSyncId());
    }


    private void clearAll() {
        processorIdToFlowTypeMap.clear();
        processorIdToFeedProcessGroupId.clear();
        processorIdToFeedProcessGroupId.clear();
        processorIdToProcessorName.clear();
        streamingFeeds.clear();
    }

    //@TODO deal with feed versions

    public synchronized void rebuildAll() {
        loaded = false;
        List<NifiFlowProcessGroup> allFlows = nifiRestClient.getFeedFlows();

        List<RegisteredTemplate> templates = null;

        templates = metadataAccess.read(() -> metadataService.getRegisteredTemplates(), MetadataAccess.SERVICE);
        Map<String, RegisteredTemplate> feedTemplatesMap = new HashMap<>();
        templates.stream().forEach(template -> {
            template.getFeedNames().stream().forEach(feedName -> {
                feedTemplatesMap.put(feedName, template);
            });
        });
        clearAll();

        //get template associated with flow to determine failure process flow ids
        allFlows.stream().forEach(nifiFlowProcessGroup -> {
            Map<String, KyloProcessorFlowType> processorFlowTypeMap = null;
            RegisteredTemplate template = feedTemplatesMap.get(nifiFlowProcessGroup.getFeedName());
            if (template != null) {
                processorFlowTypeMap = template.getProcessorFlowTypeMap();
                //  nifiFlowProcessGroup.resetProcessorsFlowType(processorFlowTypeMap);
                updateFlow(nifiFlowProcessGroup.getFeedName(), template.isStream(), nifiFlowProcessGroup);
            }
        });
        loaded = true;

    }


    public void updateFlow(FeedMetadata feed, NifiFlowProcessGroup feedProcessGroup) {
        this.updateFlow(feed, feedProcessGroup.getId(), feedProcessGroup.getProcessorMap().values());
    }

    public void updateFlow(String feedName, boolean isStream, NifiFlowProcessGroup feedProcessGroup) {
        this.updateFlow(feedName, isStream, feedProcessGroup.getId(), feedProcessGroup.getProcessorMap().values());
    }

    public void updateFlow(String feedName, boolean isStream, String feedProcessGroupId, Collection<NifiFlowProcessor> processors) {
        feedFlowIdProcessorMap.put(feedName, toFlowIdProcessorMap(processors));
        feedProcessorIdProcessorMap.put(feedName, toProcessorIdProcessorMap(processors));
        Map<String, KyloProcessorFlowType>
            processorFlowTypeMap =
            processors.stream().collect(Collectors.toMap(flowProcessor1 -> flowProcessor1.getId(), flowProcessor1 -> flowProcessor1.getProcessorFlowType()));
        processorIdToFlowTypeMap.putAll(processorFlowTypeMap);
        Set<String> failureProcessorIds = processors.stream().filter(flowProcessor -> flowProcessor.isFailure()).map(flowProcessor1 -> flowProcessor1.getId()).collect(Collectors.toSet());
        //might not be needed
        feedFailureProcessorIdMap.put(feedName, failureProcessorIds);

        Map<String, String> processorIdToProcessGroupId = new HashMap<>();
        Map<String, String> processorIdToProcessorName = new HashMap<>();
        processors.stream().forEach(flowProcessor -> {
            processorIdToProcessGroupId.put(flowProcessor.getId(), feedProcessGroupId);
            processorIdToProcessorName.put(flowProcessor.getId(), flowProcessor.getName());
        });
        this.processorIdToFeedProcessGroupId.putAll(processorIdToProcessGroupId);
        this.processorIdToProcessorName.putAll(processorIdToProcessorName);

        processorIdMap.putAll(toProcessorIdMap(processors));
        processorIdToFeedNameMap.putAll(toProcessorIdFeedNameMap(processors, feedName));
        lastUpdated = DateTimeUtil.getNowUTCTime();

        if (isStream) {
            streamingFeeds.add(feedName);
        }
        feedLastUpated.put(feedName, lastUpdated.getMillis());

    }

    public void updateFlow(FeedMetadata feed, String feedProcessGroupId, Collection<NifiFlowProcessor> processors) {
        String feedName = feed.getCategoryAndFeedName();
        this.updateFlow(feedName, feed.getRegisteredTemplate().isStream(), feedProcessGroupId, processors);
    }


    private Map<String, NifiFlowProcessor> toProcessorIdMap(Collection<NifiFlowProcessor> processors) {
        return processors.stream().collect(Collectors.toMap(NifiFlowProcessor::getId, Function.identity()));
    }

    private Map<String, String> toProcessorIdFeedNameMap(Collection<NifiFlowProcessor> processors, String feedName) {
        return processors.stream().collect(Collectors.toMap(NifiFlowProcessor::getId, name -> feedName));
    }


    private Map<String, List<NifiFlowProcessor>> toFlowIdProcessorMap(Collection<NifiFlowProcessor> processors) {
        if (processors != null && !processors.isEmpty()) {
            return processors.stream().collect(Collectors.groupingBy(NifiFlowProcessor::getFlowId));
        }
        return null;
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


    public static class CacheSummary {

        private Map<String, Integer> summary = new HashMap<>();
        private Integer cachedSyncIds;

        public static CacheSummary build(Map<String, NiFiFlowCacheSync> syncMap) {
            Map<String, Integer>
                cacheIds =
                syncMap.entrySet().stream().collect(Collectors.toMap(stringNiFiFlowCacheSyncEntry -> stringNiFiFlowCacheSyncEntry.getKey(),
                                                                     stringNiFiFlowCacheSyncEntry1 -> stringNiFiFlowCacheSyncEntry1.getValue().getSnapshot().getAddProcessorIdToFeedNameMap().size()));
            return new CacheSummary(cacheIds);
        }

        public CacheSummary() {

        }

        private CacheSummary(Map<String, Integer> cacheIds) {
            this.summary = cacheIds;
            this.cachedSyncIds = cacheIds.keySet().size();
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
