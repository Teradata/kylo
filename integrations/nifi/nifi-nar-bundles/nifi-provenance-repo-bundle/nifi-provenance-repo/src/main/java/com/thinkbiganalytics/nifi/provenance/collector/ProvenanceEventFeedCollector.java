package com.thinkbiganalytics.nifi.provenance.collector;

import com.thinkbiganalytics.nifi.provenance.model.FlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by sr186054 on 8/14/16. Group Events by Feed. The Feed is derived from looking at the first ProcessorId in the flow.
 */
public class ProvenanceEventFeedCollector extends AbstractProvenanceEventCollector implements ProvenanceEventCollector {


    /**
     * If the Event has the feedname assigned (which it should from the "CacheUtil.cacheAndBuildFlowFileGraph" then use that.
     * If for some reason the feedName is not there then look at the firstEvent in the flowFile and get the ProcessorId.
     * If for some reason that is not there then use the ProcessorId on this event.
     * Technically the feedName should always be there, but the others are included as a precaution.
     * @param event
     * @return
     */
    @Override
    public String getMapKey(ProvenanceEventRecordDTO event) {
        if(StringUtils.isNotBlank(event.getFeedName())){
            return event.getFeedName();
        }
        else {
            FlowFile flowFile = event.getFlowFile();
            if (flowFile != null && flowFile.getFirstEvent() != null) {
                return flowFile.getFirstEvent().getComponentId();
            }
            return event.getComponentId();
        }
    }

}
