package com.thinkbiganalytics.nifi.provenance.collector;

import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

/**
 * Created by sr186054 on 8/14/16.
 * Group Events by Feed.
 * The Feed is derived from looking at the first ProcessorId in the flow.
 */
public class ProvenanceEventFeedCollector extends AbstractProvenanceEventCollector implements ProvenanceEventCollector {


    @Override
    public String getMapKey(ProvenanceEventRecordDTO event) {
        ActiveFlowFile flowFile = event.getFlowFile();
        if (flowFile != null && flowFile.getFirstEvent() != null) {
            return flowFile.getFirstEvent().getComponentId();
        }
        return event.getComponentId();
    }

}
