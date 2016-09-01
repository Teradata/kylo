package com.thinkbiganalytics.nifi.provenance.v2.cache;

import com.thinkbiganalytics.nifi.provenance.ProvenanceFeedLookup;
import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventUtil;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.FlowFileGuavaCache;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.FlowFileMapDbCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility to build the FlowFile graph from an incoming Provenance Event and cache the FlowFile Graph.
 *
 * Created by sr186054 on 8/20/16.
 */
@Component
public class CacheUtil {

    private static final Logger log = LoggerFactory.getLogger(CacheUtil.class);

    @Autowired
        ProvenanceFeedLookup provenanceFeedLookup;


    // internal counters for general stats
    AtomicLong startJobCounter = new AtomicLong(0L);
    AtomicLong finishedJobCounter = new AtomicLong(0L);
    AtomicLong eventCounter = new AtomicLong(0L);

    //private static CacheUtil instance = new CacheUtil();
    //public static CacheUtil instance() {
    //    return instance;
   // }

    private CacheUtil() {

    }

    public void logStats() {
        log.info("Processed {} events.  Started Jobs {}, Finished Jobs: {}, Active Jobs: {}. ", eventCounter.get(), startJobCounter.get(), finishedJobCounter.get(),
                 (startJobCounter.get()-finishedJobCounter.get()));
    }

    /**
     * Create the FlowFile graph and cache the FlowFile with event into the GuavaCache for processing
     *
     * @param event
     */
    public void cacheAndBuildFlowFileGraph(ProvenanceEventRecordDTO event) {

        // Get the FlowFile from the Cache.  It is LoadingCache so if the file is new the Cache will create it
        FlowFileGuavaCache flowFileCache = FlowFileGuavaCache.instance();
        ActiveFlowFile flowFile = flowFileCache.getEntry(event.getFlowFileUuid());
        event.setFlowFile(flowFile);

         // Track what flow files were modified so they can be persisted until the entire Flow file is complete in case NiFi goes down while processing
        Set<ActiveFlowFile> modified = new HashSet<>();

        //An event is the very first in the flow if it is a CREATE or RECEIVE event and if there are no Parent flow files
        //This indicates the start of a Job.
        if (ProvenanceEventUtil.isFirstEvent(event) && (event.getParentUuids() == null || (event.getParentUuids() != null && event.getParentUuids().isEmpty()))) {
            flowFile.setFirstEvent(event);
            flowFile.markAsRootFlowFile();
            event.setIsStartOfJob(true);
            startJobCounter.incrementAndGet();;
            modified.add(flowFile);
        }

        //Build the graph of parent/child flow files
        if (event.getParentUuids() != null && !event.getParentUuids().isEmpty()) {
            for (String parent : event.getParentUuids()) {
                if (!flowFile.getId().equals(parent)) {
                    ActiveFlowFile parentFlowFile = flowFile.addParent(flowFileCache.getEntry(parent));
                    parentFlowFile.addChild(flowFile);
                    modified.add(flowFile);
                    modified.add(parentFlowFile);
                }
            }
        }
        if (event.getChildUuids() != null && !event.getChildUuids().isEmpty()) {
            for (String child : event.getChildUuids()) {
                ActiveFlowFile childFlowFile = flowFile.addChild(flowFileCache.getEntry(child));
                childFlowFile.addParent(flowFile);
                modified.add(flowFile);
                modified.add(childFlowFile);
            }
        }
        //assign the feed info for quick lookup on the flow file?
        boolean assignedFeedInfo = provenanceFeedLookup.assignFeedInformationToFlowFile(flowFile);
        if (!assignedFeedInfo && !flowFile.hasFeedInformationAssigned()) {
            log.error("Unable to assign Feed Info to flow file {} for event {} ", flowFile.getId(), event);
        } else {
            event.setFeedName(flowFile.getFeedName());
            event.setFeedProcessGroupId(flowFile.getFeedProcessGroupId());
        }
        event.setJobFlowFileId(flowFile.getRootFlowFile().getId());
        event.setJobEventId(flowFile.getRootFlowFile().getFirstEvent().getEventId());
        event.setProcessorName(provenanceFeedLookup.getProcessorName(event.getComponentId()));

        //If the event is the final event for the processor then add it to the flow file
        if (ProvenanceEventUtil.isCompletionEvent(event)) {
            flowFile.addCompletedEvent(event);
        }
        //update the internal counters
        if (event.isEndingFlowFileEvent() && flowFile.getRootFlowFile().isFlowComplete()) {
             finishedJobCounter.incrementAndGet();
        }

        eventCounter.incrementAndGet();
        //persist the files to disk
        for (ActiveFlowFile modifiedFlowFile : modified) {
            FlowFileMapDbCache.instance().cacheFlowFile(modifiedFlowFile);
        }

    }
}
