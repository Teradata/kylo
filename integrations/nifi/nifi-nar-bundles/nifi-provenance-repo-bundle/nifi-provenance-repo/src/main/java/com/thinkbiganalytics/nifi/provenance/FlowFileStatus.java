package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.v2.cache.flow.NifiFlowCache;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile.ActiveFlowFile;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

/**
 * Utility to check and see if an ActiveFlowFile is complete by comparing it to the Flow Process Graph and identifying if any processors still have uncompleted Destinations Created by sr186054 on
 * 8/14/16.
 */
public class FlowFileStatus {

    private static final Logger log = LoggerFactory.getLogger(FlowFileStatus.class);

    public static boolean isComplete(ActiveFlowFile flowFile) {
        NifiFlowProcessGroup flow = NifiFlowCache.instance().getFlow(flowFile);
        if (flow != null) {
            //check to see if current flowFile is complete
            Set<String> completedProcessorIds = Collections.unmodifiableSet(flowFile.getCompletedProcessorIds());
            return completedProcessorIds.stream().anyMatch(processorId -> {
                NifiFlowProcessor processor = flow.getProcessor(processorId);
                if (processor != null) {
                    Set<String> destinationIds = processor.getDestinationIds();
                    //valid if there are no destinations or if at least 1 destination has been processed.
                    if (destinationIds.isEmpty() || destinationIds.stream().anyMatch(destId -> completedProcessorIds.contains(destId))) {
                        return true;
                    }
                    return false;
                }
                return false;

            });

        } else {
            log.warn("WARNING unable to find Flow Graph for Flow File {} ", flowFile.getId());
        }
        //unable to get flow??
        //what to do??
        return false;
    }

}
