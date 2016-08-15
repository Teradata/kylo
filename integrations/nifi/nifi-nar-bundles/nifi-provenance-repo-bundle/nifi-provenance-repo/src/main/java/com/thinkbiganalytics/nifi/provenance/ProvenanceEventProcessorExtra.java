package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flow.NifiFlowCache;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.FlowFileCache;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some code that may be used in the future Created by sr186054 on 8/14/16.
 */
public class ProvenanceEventProcessorExtra {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventProcessorExtra.class);


    public void process(ProvenanceEventRecordDTO event) {
        boolean processed = false;
        //the flow file that makes up the processing for this event(s)
        ActiveFlowFile eventFlowFile = FlowFileCache.instance().getEntry(event.getFlowFileUuid());

        //the root flow file that makes up the entire flow /job
        ActiveFlowFile rootFlowFile = eventFlowFile.getRootFlowFile();

        if (rootFlowFile != null) {
            //get the flow Graph
            //1 get the processGroup for the flow

            //  log.info("ATTEMPT TO GET GRAPH for this Flow {}, {} ",rootFlowFile.getFirstEvent().getComponentId(), rootFlowFile.getFirstEvent().getEventTime());
            try {
                NifiFlowProcessor startingProcessor = NifiFlowCache.instance().getStartingProcessor(rootFlowFile.getFirstEvent().getComponentId());
                if (startingProcessor != null) {
                    NifiFlowProcessor processor = NifiFlowCache.instance().getProcessor(startingProcessor.getProcessGroup().getId(), event.getComponentId());
                    if (processor != null) {
                        if (processor.isFailure()) {
                            //process failure
                            //if this event is a failure or the previous one?
                            eventFlowFile.addFailedEvent(event);
                        }
                        if (processor.isEnd()) {
                            //process end
                            rootFlowFile.completeEndingProcessor();
                            eventFlowFile.completeEndingProcessor();

                        }
                        processed = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            //TODO Handling processing events out of order when the first event is not processed yet...
        }
        if (!processed) {
            log.info("Unable to Process EVENT {}, {} ", event.getEventId(), event.getComponentId());
        } else {
            log.info("PROCESSED EVENT {}, {} ", event.getEventId(), event.getComponentId());
        }
        //calc running stats on processor?

    }

    public void generateStats(ProvenanceEventRecord event) {
        //get the flow Graph

        //the flow file that makes up the processing for this event(s)
        ActiveFlowFile eventFlowFile = FlowFileCache.instance().getEntry(event.getFlowFileUuid());

        //the root flow file that makes up the entire flow /job
        ActiveFlowFile rootFlowFile = eventFlowFile.getRootFlowFile();


    }


}
