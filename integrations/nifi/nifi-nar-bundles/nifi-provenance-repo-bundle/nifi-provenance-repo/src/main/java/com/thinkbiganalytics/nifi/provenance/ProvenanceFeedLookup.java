package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.model.FlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flow.NifiFlowCache;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.FlowFileMapDbCache;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public  boolean assignFeedInformationToFlowFile(FlowFile flowFile) {
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
                FlowFile rootFlowFile = flowFile.getRootFlowFile();
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

}
