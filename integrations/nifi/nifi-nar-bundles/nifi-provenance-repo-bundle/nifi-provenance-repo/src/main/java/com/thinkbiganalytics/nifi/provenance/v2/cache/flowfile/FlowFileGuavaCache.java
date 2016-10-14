package com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.nifi.provenance.AggregationEventProcessingStats;
import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.IdReferenceFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.v2.cache.CacheUtil;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flow.NifiFlowCache;
import com.thinkbiganalytics.util.SpringApplicationContext;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * A cache of Flowfiles active in the system
 * The FlowFile Graph is build when a ProvenanceEvent comes into the system via the DelayedProvenanceEventProducer which delegates to the CacheUtil to get/build the graph
 * In addition to the in memory flowfile cache the system is also storing a lightweight IdReference Cache to disk.
 * If Nifi goes down during processing the Flowfiles will be restored from this disk cache and the graph will be rebuilt.
 * If the attempt to get the flowfile from this guava cache is not there the system will first attempt to find that item in the disk cache and build/return the object to the guava cache, otherwise it will create a new flowfile object
 *
 * A timer runs on a given interval to invalidate and remove flowfiles that are finished
 *
 * Created by sr186054 on 8/11/16.
 */
@Component
public class FlowFileGuavaCache {

    private static final Logger log = LoggerFactory.getLogger(FlowFileGuavaCache.class);

    @Value("${thinkbig.provenance.flowfile.cache.expire.seconds:10}")
    private Integer expireTimerCheckSeconds = 10;

    @Autowired
    FlowFileMapDbCache flowFileMapDbCache;

    @Autowired
    NifiFlowCache nifiFlowCache;


    @Autowired
    CacheUtil cacheUtil;

    public static FlowFileGuavaCache instance() {
        return (FlowFileGuavaCache) SpringApplicationContext.getInstance().getBean("flowFileGuavaCache");
    }

    private final LoadingCache<String, ActiveFlowFile> cache;

    private DateTime lastPrintLogTime = null;
    private Long PRINT_LOG_MILLIS = 60 * 2 * 1000L; //print the summary log every 2 minutes

    private FlowFileGuavaCache() {
        cache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, ActiveFlowFile>() {
                                                                  @Override
                                                                  public ActiveFlowFile load(String id) throws Exception {
                                                                      return loadFromCache(id);
                                                                  }

                                                              }
        );
        log.info("Created new FlowFileGuavaCache running timer every {} seconds to check and expire finished flow files", expireTimerCheckSeconds);
        initTimerThread();
    }


    /**
     * Load the FlowFile from the saved "IdReference MapDB " or creates a new FlowFile object
     * @param parentFlowFile
     * @return
     */
    private ActiveFlowFile loadGraph(IdReferenceFlowFile parentFlowFile) {
        ActiveFlowFile parent = cache.getIfPresent(parentFlowFile.getId());
        if (parentFlowFile != null) {
            if (parent == null) {
                parent = new ActiveFlowFile(parentFlowFile.getId());
                parent.assignFeedInformation(parentFlowFile.getFeedName(), parentFlowFile.getFeedProcessGroupId());
                if (parentFlowFile.isRootFlowFile()) {
                    parent.markAsRootFlowFile();
                    ProvenanceEventRecordDTO firstEvent = constructFirstEvent(parentFlowFile);
                    parent.setFirstEvent(firstEvent);
                    firstEvent.setFlowFile(parent);

                }
                parent.setCurrentFlowFileComplete(parentFlowFile.isComplete());
                cache.put(parent.getId(), parent);
            }


            for (String childId : parentFlowFile.getChildIds()) {
                ActiveFlowFile child = cache.getIfPresent(childId);
                if (child == null) {
                    IdReferenceFlowFile idReferenceFlowFile = flowFileMapDbCache.getCachedFlowFile(childId);
                    child = loadGraph(idReferenceFlowFile);
                    parent.addChild(child);
                    child.addParent(parent);
                    cache.put(childId, child);
                }
            }
        }
        return parent;
    }

    /**
     * Construct what is needed from the idRef file to create the First event of the root flow file
     * @param rootFlowFile
     * @return
     */
    private ProvenanceEventRecordDTO constructFirstEvent(IdReferenceFlowFile rootFlowFile){
        ProvenanceEventRecordDTO firstEvent = new ProvenanceEventRecordDTO();
        firstEvent.setEventId(rootFlowFile.getRootFlowFileFirstEventId());
        firstEvent.setEventTime(new DateTime(rootFlowFile.getRootFlowFileFirstEventTime()));
        firstEvent.setEventType(rootFlowFile.getRootFlowFileFirstEventType());
        firstEvent.setComponentId(rootFlowFile.getRootFlowFileFirstEventComponentId());
        firstEvent.setComponentName(rootFlowFile.getRootFlowFileFirstEventComponentName());
        firstEvent.setFeedName(rootFlowFile.getFeedName());
        firstEvent.setFeedProcessGroupId(rootFlowFile.getFeedProcessGroupId());

        return firstEvent;
    }

    private ActiveFlowFile loadFromCache(String flowFileId) {
        ActiveFlowFile ff = null;
        IdReferenceFlowFile idReferenceFlowFile = flowFileMapDbCache.getCachedFlowFile(flowFileId);
        if (idReferenceFlowFile != null) {
            //try to get the root file and build the graph and then return this ff
            if (StringUtils.isNotBlank(idReferenceFlowFile.getRootFlowFileId())) {
                ActiveFlowFile rootFile = cache.getIfPresent(idReferenceFlowFile.getRootFlowFileId());
                if (rootFile == null) {
                    IdReferenceFlowFile root = flowFileMapDbCache.getCachedFlowFile(idReferenceFlowFile.getRootFlowFileId());
                    loadGraph(root);
                }
            }
            ff = (ActiveFlowFile) cache.getIfPresent(flowFileId);
        }
        if (ff == null) {
            ff = new ActiveFlowFile(flowFileId);
        } else {
            log.info("LOADED FlowFile from cached id map {} ", ff);
        }
        return ff;
    }

    public ActiveFlowFile getEntry(String id) {
        return cache.getUnchecked(id);
    }


    public List<ActiveFlowFile> getRootFlowFiles() {
        return cache.asMap().values().stream().filter(flowFile -> flowFile.isRootFlowFile()).collect(Collectors.toList());
    }

    public List<ActiveFlowFile> getCompletedRootFlowFiles() {
        int backoffSeconds = 60;
        DateTime completeTime = DateTime.now().minusSeconds(backoffSeconds);
        //hold onto any completed flow files for xx seconds after they are complete before removing from cache
        return cache.asMap().values().stream().filter(
            flowFile -> (flowFile.isRootFlowFile() && flowFile.getRootFlowFile().isCanExpire() && completeTime.isAfter(flowFile.getRootFlowFile().getMinimiumExpireTime())))
            .collect(
                Collectors.toList());
    }

    public CacheStats stats() {
        return cache.stats();
    }

    public void printSummary() {
        Map<String, ActiveFlowFile> map = cache.asMap();
        List<ActiveFlowFile> rootFiles = getRootFlowFiles();
        cacheUtil.logStats();
        log.info("FLOW FILE Cache Size: {} , root files {}, processorNameMapSize: {} ", map.size(), rootFiles.size(), nifiFlowCache.processorNameMapSize());
        log.info("JMS Stats:  Sent {} streaming events to JMS.  Sent {} batch events to JMS ", AggregationEventProcessingStats.getStreamingEventsSent(), AggregationEventProcessingStats.getBatchEventsSent());
        flowFileMapDbCache.summary();


    }

    public void invalidate(ActiveFlowFile flowFile) {
        if (flowFile.getRootFlowFile().isFlowComplete()) {
            //EventMapDbCache.instance().expire(flowFile);
            flowFileMapDbCache.expire(flowFile);
            cache.invalidate(flowFile.getId());
            //also invalidate all children
            flowFile.getChildren().forEach(child -> invalidate(child));
        }
    }


    public void expire() {
        try {
            long start = System.currentTimeMillis();
            List<ActiveFlowFile> rootFiles = getCompletedRootFlowFiles();
            if (!rootFiles.isEmpty()) {
                for (ActiveFlowFile root : rootFiles) {
                    invalidate(root);
                }
                long stop = System.currentTimeMillis();
                if (rootFiles.size() > 0) {
                    log.info("Time to expire {} flowfiles {} ms. Root Flow Files left in cache: {} ", rootFiles.size(), (stop - start), getRootFlowFiles().size());
                }
            }
            if(lastPrintLogTime == null || (lastPrintLogTime != null && DateTime.now().getMillis() - lastPrintLogTime.getMillis() > (PRINT_LOG_MILLIS))) {
                printSummary();
                lastPrintLogTime = DateTime.now();
            }

        } catch (Exception e) {
            log.error("Error attempting to invalidate FlowFileGuava cache {}, {}", e.getMessage(), e);
        }
    }

    private void initTimerThread() {
        ScheduledExecutorService service = Executors
            .newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            expire();
        }, expireTimerCheckSeconds, expireTimerCheckSeconds, TimeUnit.SECONDS);


    }


}
