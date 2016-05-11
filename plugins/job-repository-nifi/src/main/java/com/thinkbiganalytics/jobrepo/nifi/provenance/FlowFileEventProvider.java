package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileEvents;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;

/**
 * Created by sr186054 on 5/9/16.
 */
public interface FlowFileEventProvider {
    FlowFileEvents addFlowFile(String flowFileId);

    void addEvent(ProvenanceEventRecordDTO event);

    FlowFileEvents getFlowFile(String flowFileId);

    FlowFileEvents getOrAddFlowFile(String flowFileId);

}
