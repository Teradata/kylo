package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileEvents;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;

import java.util.*;

/**
 * Created by sr186054 on 5/9/16.
 */
public class InMemoryFlowFileEventProvider implements FlowFileEventProvider {


    protected Map<String, FlowFileEvents> flowFileMap = new HashMap<>();
    protected Set<ProvenanceEventRecordDTO> events = new HashSet<>();

    public InMemoryFlowFileEventProvider() {

    }

    @Override
    public FlowFileEvents addFlowFile(String flowFileId) {
        if (!flowFileMap.containsKey(flowFileId)) {
            flowFileMap.put(flowFileId, new FlowFileEvents(flowFileId));
        }
        return getFlowFile(flowFileId);
    }

    @Override
    public void addEvent(ProvenanceEventRecordDTO event) {
        events.add(event);
    }


    @Override
    public FlowFileEvents getFlowFile(String flowFileId) {
        FlowFileEvents flowFile = flowFileMap.get(flowFileId);
        return flowFile;
    }

    @Override
    public FlowFileEvents getOrAddFlowFile(String flowFileId) {
        return addFlowFile(flowFileId);
    }


}
