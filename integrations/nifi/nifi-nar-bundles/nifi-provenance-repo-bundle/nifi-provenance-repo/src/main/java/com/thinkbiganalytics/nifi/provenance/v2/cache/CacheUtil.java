package com.thinkbiganalytics.nifi.provenance.v2.cache;

import com.thinkbiganalytics.nifi.provenance.ProvenanceFeedLookup;
import com.thinkbiganalytics.nifi.provenance.model.FlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.util.ProvenanceEventUtil;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flow.NifiFlowCache;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.EventMapDbCache;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.FlowFileCache;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.FlowFileGuavaCache;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.FlowFileMapDbCache;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 8/20/16.
 */
public class CacheUtil {

    private static final Logger log = LoggerFactory.getLogger(CacheUtil.class);

    AtomicLong startJobCounter = new AtomicLong(0L);
    AtomicLong dropEventCounter = new AtomicLong(0L);
    AtomicLong activeJobCounter = new AtomicLong(0L);
    AtomicLong eventCounter = new AtomicLong(0L);
    private DateTime minEventTime;

    private static CacheUtil instance = new CacheUtil();

    public static CacheUtil instance() {
        return instance;
    }

    private CacheUtil() {

    }

    public void logStats() {
        log.info("Processed {} events.  Started Jobs {}, Finished Jobs: {}, Active Jobs: {}.  MinEventTime: {}  ", eventCounter.get(), startJobCounter.get(), dropEventCounter.get(),
                 activeJobCounter.get(), minEventTime);
    }

    public void cache(ProvenanceEventRecordDTO event) {
        if (minEventTime == null) {
            minEventTime = event.getEventTime();
        }
        if (minEventTime.isAfter(event.getEventTime())) {
            minEventTime = event.getEventTime();
        }

        //convert it to a dto so we can add additional tracking properties

        FlowFileCache flowFileCache = FlowFileGuavaCache.instance();

        FlowFile flowFile = flowFileCache.getEntry(event.getFlowFileUuid());
        Set<FlowFile> modified = new HashSet<>();

        event.setFlowFile(flowFile);

        //An event is the very first in the flow if it is a CREATE or RECEIVE event and if there are no Parent flow files
        if (ProvenanceEventUtil.isFirstEvent(event) && (event.getParentUuids() == null || (event.getParentUuids() != null && event.getParentUuids().isEmpty()))) {
            flowFile.setFirstEvent(event);
            flowFile.markAsRootFlowFile();
            startJobCounter.incrementAndGet();
            activeJobCounter.incrementAndGet();
            modified.add(flowFile);
            //       log.info("marking flow file as root {} ", flowFile.getId());
        }

        if (event.getParentUuids() != null && !event.getParentUuids().isEmpty()) {
            for (String parent : event.getParentUuids()) {
                if (!flowFile.getId().equals(parent)) {
                    //      log.info("adding parent {} to child {} ", parent, flowFile.getId());
                    FlowFile parentFlowFile = flowFile.addParent(flowFileCache.getEntry(parent));
                    parentFlowFile.addChild(flowFile);
                    modified.add(flowFile);
                    modified.add(parentFlowFile);
                }
            }
        }

        if (event.getChildUuids() != null && !event.getChildUuids().isEmpty()) {
            for (String child : event.getChildUuids()) {
                //     log.info("adding child {} to parent {} ", child, flowFile.getId());
                FlowFile childFlowFile = flowFile.addChild(flowFileCache.getEntry(child));
                childFlowFile.addParent(flowFile);
                modified.add(flowFile);
                modified.add(childFlowFile);
            }
        }
        //assign the feed info for quick lookup on the flow file?
        boolean assignedFeedInfo = ProvenanceFeedLookup.assignFeedInformationToFlowFile(flowFile);
        if (!assignedFeedInfo && !flowFile.hasFeedInformationAssigned()) {
            log.error("Unable to assign Feed Info to flow file {} for event {} ", flowFile.getId(), event);
        } else {
            event.setFeedName(flowFile.getFeedName());
        }
        //finalize the event if it is not pulled from loading map
        if (ProvenanceEventUtil.isCompletionEvent(event)) {
            flowFile.addCompletedEvent(event);
        }
        if (event.isEndingEvent()) {
            dropEventCounter.incrementAndGet();
            // if(flowFile.isFlowComplete()) {
            //     activeJobCounter.decrementAndGet();
            //  }
        }
        eventCounter.incrementAndGet();
        for (FlowFile modifiedFlowFile : modified) {
            FlowFileMapDbCache.instance().cacheFlowFile(modifiedFlowFile);
        }

    }

    public static void bootstrapCache() {
        NifiFlowCache.instance(); //.loadAll();
        EventMapDbCache.instance();
    }
}
