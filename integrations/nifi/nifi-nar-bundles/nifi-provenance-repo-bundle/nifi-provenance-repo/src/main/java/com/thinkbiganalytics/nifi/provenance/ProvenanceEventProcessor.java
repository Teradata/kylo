package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.v2.cache.event.ProvenanceEventCache;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.FlowFileCache;

import org.apache.nifi.provenance.ProvenanceEventRecord;

/**
 * Created by sr186054 on 8/11/16. Process flow
 */
public class ProvenanceEventProcessor {


    public void process(ProvenanceEventRecord event) {
        ProvenanceEventCache.instance().put(event);
        ActiveFlowFile flowFile = FlowFileCache.instance().getEntry(event.getFlowFileUuid());
        flowFile.addEvent(event.getEventId());
        if (ProvenanceEventCache.isFirstEvent(event)) {
            flowFile.setFirstEvent(event.getEventId());
        }

        if (event.getParentUuids() != null && !event.getParentUuids().isEmpty()) {
            for (String parent : event.getParentUuids()) {
                ActiveFlowFile parentFlowFile = flowFile.addParent(FlowFileCache.instance().getEntry(parent));
                parentFlowFile.addChild(flowFile);
            }
        }

        if (event.getChildUuids() != null && !event.getChildUuids().isEmpty()) {
            for (String child : event.getChildUuids()) {
                ActiveFlowFile childFlowFile = flowFile.addParent(FlowFileCache.instance().getEntry(child));
                childFlowFile.addParent(flowFile);
            }
        }

        //reference the FlowFileCache to do intelligent things about sending and aggregrating the events.

    }


}
