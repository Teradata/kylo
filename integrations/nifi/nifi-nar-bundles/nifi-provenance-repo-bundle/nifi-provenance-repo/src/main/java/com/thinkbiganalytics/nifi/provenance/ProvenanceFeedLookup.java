package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.stats.ProvenanceEventStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.StatsModel;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flow.NifiFlowCache;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.FlowFileMapDbCache;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sr186054 on 8/18/16.
 */
@Component
public class ProvenanceFeedLookup {

    @Autowired
    private NifiFlowCache nifiFlowCache;

    private static final Logger log = LoggerFactory.getLogger(ProvenanceFeedLookup.class);

    public  String getFeedNameForEvent(ProvenanceEventRecordDTO event) {
        String feedName = null;
        NifiFlowProcessGroup flow = nifiFlowCache.getFlow(event.getFlowFile());
        if (flow != null) {
            feedName = flow.getFeedName();
        }
        return feedName;
    }

    public  String getFeedProcessGroup(ProvenanceEventRecordDTO event) {
        String feedProcessGroup = null;
        NifiFlowProcessGroup flow = nifiFlowCache.getFlow(event.getFlowFile());
        if (flow != null) {
            feedProcessGroup = flow.getId();
        }
        return feedProcessGroup;
    }

    public  NifiFlowProcessGroup getFeedFlow(ProvenanceEventRecordDTO event) {
        NifiFlowProcessGroup flow = nifiFlowCache.getFlow(event.getFlowFile());
        return flow;
    }

    public boolean ensureProcessorIsInCache(String processGroupId, String processorId) {
        String processorName = getProcessorName(processorId);
        if (StringUtils.isBlank(processorName)) {
            log.info("Unable to get ProcessorName for {},{}.  Attempt to look it up via rest client ", processGroupId, processorId);
            nifiFlowCache.getProcessor(processGroupId, processorId);
            processorName = getProcessorName(processorId);
            if (StringUtils.isNotBlank(processorName)) {
                log.info("Sucessfully got ProcessorName {} for {},{}.  Attempt to look it up via rest client ", processorName, processGroupId, processorId);
            }
        }
        return StringUtils.isNotBlank(processorName);
    }

    public String getProcessorName(String processorId) {
        return nifiFlowCache.getProcessorName(processorId);
    }

    public NifiFlowProcessor getProcessor(String feedProcessGroupId, String processorId) {
        return nifiFlowCache.getProcessor(feedProcessGroupId, processorId);
    }


    public boolean assignFeedInformationToFlowFile(ActiveFlowFile flowFile) {
        boolean assigned = false;
        //attempt to assign it from the cache first
        FlowFileMapDbCache.instance().assignFeedInformation(flowFile);
        if (!flowFile.hasFeedInformationAssigned() && flowFile.getRootFlowFile() != null) {
            //check to see if any of the parents in this flow already have it assigned.. if so no need to look it up
            if (flowFile.getRootFlowFile().hasFeedInformationAssigned()) {
                flowFile.assignFeedInformation(flowFile.getRootFlowFile().getFeedName(), flowFile.getRootFlowFile().getFeedProcessGroupId());
                assigned = flowFile.hasFeedInformationAssigned();
            }
            if (!flowFile.hasFeedInformationAssigned()) {
                ActiveFlowFile rootFlowFile = flowFile.getRootFlowFile();
                    NifiFlowProcessGroup flow = nifiFlowCache.getFlow(rootFlowFile);
                    if (flow != null) {
                        flowFile.assignFeedInformation(flow.getFeedName(), flow.getId());
                        assigned = flowFile.hasFeedInformationAssigned();
                        if (assigned && !rootFlowFile.hasFeedInformationAssigned()) {
                            rootFlowFile.assignFeedInformation(flow.getFeedName(), flow.getId());
                        }
                    } else {
                        log.error(" Unable to get Feed Name and flow/graph for Flow file: {}, rootFlowFile: {} ", flowFile, rootFlowFile);
                    }

            }
        }
        if (flowFile.getRootFlowFile() == null) {
            log.error("ERROR ROOT FLOW FILE IS EMPTY for flowFile {} with parents: {} ", flowFile.getId(), flowFile.getParents().size());
        }
        return assigned;
    }

    public Set<ProvenanceEventRecordDTO> getFailureEvents(ProvenanceEventRecordDTO event) {
        Set<ProvenanceEventRecordDTO> failureEvents = new HashSet<>();
        if (event.isTerminatedByFailureRelationship()) {
            failureEvents.add(event);
        }
        ProvenanceEventRecordDTO connectedFailure = getFailureProvenanceEventFromConnectionRelationship(event);
        if (connectedFailure != null) {
            failureEvents.add(connectedFailure);
        }
        return failureEvents;
    }

    public boolean isEventFromFailureConnection(ProvenanceEventRecordDTO event) {
        if (StringUtils.isNotBlank(event.getSourceConnectionIdentifier())) {
            String realFailureProcessorId = nifiFlowCache.getFailureProcessorWithDestinationConnectionIdentifier(event.getSourceConnectionIdentifier());
            return StringUtils.isNotBlank(realFailureProcessorId);
        }
        return false;
    }

    public ProvenanceEventRecordDTO getFailureProvenanceEventFromConnectionRelationship(ProvenanceEventRecordDTO event) {
        ProvenanceEventRecordDTO failureEvent = null;
            if (StringUtils.isNotBlank(event.getSourceConnectionIdentifier())) {
                String realFailureProcessorId = nifiFlowCache.getFailureProcessorWithDestinationConnectionIdentifier(event.getSourceConnectionIdentifier());
                if (StringUtils.isNotBlank(realFailureProcessorId)) {
                    final String finalRealFailureProcessorId = realFailureProcessorId;
                    ProvenanceEventRecordDTO matchingFailedEvent = null;
                    if (event.getPreviousEvent() != null && event.getPreviousEvent().getComponentId().equalsIgnoreCase(realFailureProcessorId)) {
                        matchingFailedEvent = event.getPreviousEvent();
                    } else {
                        matchingFailedEvent = event.getFlowFile().getFirstCompletedEventsForProcessorId(finalRealFailureProcessorId);
                        //previous flow files??
                        log.info("Matching Event in flowfile = {}", matchingFailedEvent);
                    }

                    if (matchingFailedEvent != null) {
                        failureEvent = matchingFailedEvent;
                    }
                }
            }

        return failureEvent;
    }

    public ProvenanceEventStats failureEventStats(ProvenanceEventRecordDTO failureEvent) {
        /// now transform the failure event into a Stats record to be included in
        ProvenanceEventStats stats = StatsModel.toFailureProvenanceEventStats(failureEvent.getFeedName(), failureEvent);
        return stats;
    }


}
