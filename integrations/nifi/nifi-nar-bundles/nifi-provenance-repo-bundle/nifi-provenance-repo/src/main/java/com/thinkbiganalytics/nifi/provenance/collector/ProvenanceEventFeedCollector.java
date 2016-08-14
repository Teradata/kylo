package com.thinkbiganalytics.nifi.provenance.collector;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.ActiveFlowFile;

/**
 * Created by sr186054 on 8/14/16.
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
