package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.v2.cache.flow.NifiFlowCache;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.flow.NifiFlowProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility to check and see if an ActiveFlowFile is complete by comparing it to the Flow Process Graph and identifying if any processors still have uncompleted Destinations Created by sr186054 on
 * 8/14/16.
 */
public class FlowFileStatus {

    private static final Logger log = LoggerFactory.getLogger(FlowFileStatus.class);

    public static boolean isComplete(ActiveFlowFile flowFile) {
        NifiFlowProcessGroup flow = NifiFlowCache.instance().getFlow(flowFile);
        return isComplete(flowFile, flow);
    }

    /**
     * TODO.  THIS CLASS STILL NEEDS work.  doesnt work right
     */
    public static boolean isComplete(ActiveFlowFile flowFile, NifiFlowProcessGroup flow) {
        if (flow != null) {
            //check to see if current flowFile is complete
            final Set<String> completedProcessorIds = new HashSet<>(flowFile.getCompletedProcessorIds());
            return completedProcessorIds.stream().anyMatch(processorId -> {
                NifiFlowProcessor processor = flow.getProcessor(processorId);
                if (processor != null) {
                    Set<String> destinationIds = processor.getAllDestinationIds();
                    //valid if there are no destinations or if all destinations has been processed and there are no destinations
                    if (destinationIds.stream().allMatch(destId -> completedProcessorIds.contains(destId))) {
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
