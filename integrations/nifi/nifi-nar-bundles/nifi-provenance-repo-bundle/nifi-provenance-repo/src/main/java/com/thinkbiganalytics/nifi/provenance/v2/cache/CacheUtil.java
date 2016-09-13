package com.thinkbiganalytics.nifi.provenance.v2.cache;

import com.google.common.collect.Sets;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventProcessingException;
import com.thinkbiganalytics.nifi.provenance.ProvenanceFeedLookup;
import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.RootFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventUtil;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.FlowFileGuavaCache;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.FlowFileMapDbCache;

import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    FlowFileGuavaCache flowFileGuavaCache;

    @Autowired
    FlowFileMapDbCache flowFileMapDbCache;

    // internal counters for general stats
    AtomicLong eventCounter = new AtomicLong(0L);

    private CacheUtil() {

    }

    public void logStats() {
        log.info("Processed {} events.  ", eventCounter.get());
    }

    /**
     * Create the FlowFile graph and cache the FlowFile with event into the GuavaCache for processing
     *
     * @param event
     */
    public void cacheAndBuildFlowFileGraph(ProvenanceEventRecordDTO event) {

        // Get the FlowFile from the Cache.  It is LoadingCache so if the file is new the Cache will create it
        FlowFileGuavaCache flowFileCache = flowFileGuavaCache;
        ActiveFlowFile flowFile = flowFileCache.getEntry(event.getFlowFileUuid());
        event.setFlowFile(flowFile);

         // Track what flow files were modified so they can be persisted until the entire Flow file is complete in case NiFi goes down while processing
        Set<ActiveFlowFile> modified = new HashSet<>();

        //An event is the very first in the flow if it is a CREATE or RECEIVE event and if there are no Parent flow files
        //This indicates the start of a Job.
        if (ProvenanceEventUtil.isFirstEvent(event) && (event.getParentUuids() == null || (event.getParentUuids() != null && event.getParentUuids().isEmpty()))) {
            flowFile.setFirstEvent(event);
            log.info("Marking {} as root file from event type {} ", flowFile.getId(), event.getEventType());
            flowFile.markAsRootFlowFile();
            event.setIsStartOfJob(true);
            modified.add(flowFile);
        }

        //Track any related Parents that are root flow files and link the jobs together
        //anything in this list should be linked together
        Set<String> relatedRootFlowFileIds = new HashSet<>();
        Set<RootFlowFile> relatedRootFlowFiles = new HashSet<>();

        //Build the graph of parent/child flow files
        if (event.getParentUuids() != null && !event.getParentUuids().isEmpty()) {

            for (String parent : event.getParentUuids()) {
                if (!flowFile.getId().equals(parent)) {
                    ActiveFlowFile parentFlowFile = flowFile.addParent(flowFileCache.getEntry(parent));
                    if(parentFlowFile.isRootFlowFile()){
                        relatedRootFlowFileIds.add(parentFlowFile.getId());
                        relatedRootFlowFiles.add(parentFlowFile.getRootFlowFile());
                    }
                    parentFlowFile.addChild(flowFile);
                    if (!flowFile.isRootFlowFile()) {
                        flowFile.getRootFlowFile().addRootFileActiveChild(flowFile.getId());
                    }
                    modified.add(flowFile);
                    modified.add(parentFlowFile);
                }
            }
        }
        if (event.getChildUuids() != null && !event.getChildUuids().isEmpty()) {
            for (String child : event.getChildUuids()) {
                ActiveFlowFile childFlowFile = flowFile.addChild(flowFileCache.getEntry(child));
                childFlowFile.addParent(flowFile);
                if (!flowFile.isRootFlowFile()) {
                    flowFile.getRootFlowFile().addRootFileActiveChild(childFlowFile.getId());
                }
                modified.add(flowFile);
                modified.add(childFlowFile);
            }
        }
        //link the root files if they exist
        //This events Root Flow file is related to these other root flow files
        event.setRelatedRootFlowFiles(relatedRootFlowFileIds);
        if (!relatedRootFlowFiles.isEmpty()) {
            relatedRootFlowFiles.forEach(rootFlowFile -> {
                rootFlowFile.addRelatedRootFlowFiles(Sets.newHashSet(relatedRootFlowFiles));
            });
        }


        if (flowFile.getRootFlowFile() != null && StringUtils.isNotBlank(flowFile.getRootFlowFile().getFeedProcessGroupId())) {
            provenanceFeedLookup.ensureProcessorIsInCache(flowFile.getRootFlowFile().getFeedProcessGroupId(), event.getComponentId());
        }
        event.setComponentName(provenanceFeedLookup.getProcessorName(event.getComponentId()));
        //assign the feed info for quick lookup on the flow file?
        boolean assignedFeedInfo = provenanceFeedLookup.assignFeedInformationToFlowFile(flowFile);
        if (!assignedFeedInfo && !flowFile.hasFeedInformationAssigned()) {
            log.error("Unable to assign Feed Info to flow file {}, root: {}, for event {} ", flowFile.getId(), flowFile.getRootFlowFile(), event);
        } else {
            event.setFeedName(flowFile.getFeedName());
            event.setFeedProcessGroupId(flowFile.getFeedProcessGroupId());
        }
        //if we dont have a root flow file assigned the we cant proceed... error out
        if (flowFile.getRootFlowFile() == null) {
            throw new ProvenanceEventProcessingException("Unable to find Root Flow File for FlowFile: " + flowFile + " and Event " + event);
        }


        event.setJobFlowFileId(flowFile.getRootFlowFile().getId());
        event.setJobEventId(flowFile.getRootFlowFile().getFirstEvent().getEventId());

        if (ProvenanceEventUtil.isCompletionEvent(event)) {
            flowFile.addCompletionEvent(event);
        }


        eventCounter.incrementAndGet();
        //persist the files to disk
        for (ActiveFlowFile modifiedFlowFile : modified) {
            flowFileMapDbCache.cacheFlowFile(modifiedFlowFile);
        }

    }
}
