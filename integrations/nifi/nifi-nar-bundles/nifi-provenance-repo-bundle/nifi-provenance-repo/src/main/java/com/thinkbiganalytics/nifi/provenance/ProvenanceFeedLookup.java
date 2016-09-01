package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.stats.ProvenanceEventStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.StatsModel;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flow.NifiFlowCache;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.FlowFileMapDbCache;
import com.thinkbiganalytics.nifi.rest.model.flow.NiFiFlowProcessorConnection;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
                if (rootFlowFile != null) {
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
        }
        return assigned;
    }

    public ProvenanceEventRecordDTO getRealFailureProvenanceEvent(ProvenanceEventRecordDTO event) {
        ProvenanceEventRecordDTO failureEvent = null;
        if (event.isFailure()) {
            if (StringUtils.isNotBlank(event.getSourceConnectionIdentifier())) {
                String realFailureProcessorId = nifiFlowCache.getFailureProcessorWithDestinationConnectionIdentifier(event.getSourceConnectionIdentifier());
                if (StringUtils.isBlank(realFailureProcessorId)) {
                    List<NifiFlowProcessor> processorsWithConnection = nifiFlowCache.getProcessorWithDestinationConnectionIdentifier(event.getSourceConnectionIdentifier());
                    log.info("Failure detected for event: {} with connection id of {} found {} processorsWith dest conn: {} ", event, event.getSourceConnectionIdentifier(),
                             (processorsWithConnection != null ? processorsWithConnection.size() : 0));
                    if (processorsWithConnection != null && !processorsWithConnection.isEmpty()) {
                        //get the first one??
                        NifiFlowProcessor processor = processorsWithConnection.get(0);
                        NiFiFlowProcessorConnection
                            conn =
                            processor.getDestinationConnectionIds().stream()
                                .filter(niFiFlowProcessorConnection -> niFiFlowProcessorConnection.getConnectionIdentifier().equalsIgnoreCase(event.getSourceConnectionIdentifier()) && StringUtils
                                    .join(niFiFlowProcessorConnection.getSelectedRelationships(), ",").toLowerCase().contains("failure")).findFirst()
                                .orElse(null);
                        log.info("NiFiFlowProcessorConnection = {} ", conn);
                        if (conn != null) {
                            //the processor that failed was this one
                            //cache the sourceconnectionID to this processorId for future reference
                            nifiFlowCache.putFailureProcessorConnectionIdentifier(event.getSourceConnectionIdentifier(), processor.getId());
                            realFailureProcessorId = processor.getId();
                        }
                    }
                }
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

            if (failureEvent == null) {
                failureEvent = event;
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
