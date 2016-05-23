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
    public FlowFileEvents getFlowFile(String flowFileId) {
        FlowFileEvents flowFile = flowFileMap.get(flowFileId);
        return flowFile;
    }

    @Override
    public FlowFileEvents getOrAddFlowFile(String flowFileId) {
        return addFlowFile(flowFileId);
    }

    public void removeFlowFile(String flowFileId){
        //remove parent and all children
        if (flowFileMap.containsKey(flowFileId)) {
            FlowFileEvents flowFile = flowFileMap.get(flowFileId);
            for(FlowFileEvents child: flowFile.getChildren()) {
                removeFlowFile(child.getUuid());
            }
            flowFileMap.remove(flowFileId);
        }
    }


}
