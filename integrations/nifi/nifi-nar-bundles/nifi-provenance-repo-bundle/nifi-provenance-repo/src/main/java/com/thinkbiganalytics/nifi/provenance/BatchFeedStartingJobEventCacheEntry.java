package com.thinkbiganalytics.nifi.provenance;

/*-
 * #%L
 * thinkbig-nifi-provenance-repo
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.joda.time.DateTime;

import java.util.Set;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import java.util.HashSet;

/**
 *
 */
public class BatchFeedStartingJobEventCacheEntry extends BatchFeedProcessorEventCacheEntry {

    /**
     * Set of starting flow file ids that are related
     * This populated when multiple starting events come in as a stream
     */
    protected Set<String> startingFlowFileIds;

    /**
     * The primary flow file id.
     * This is used for Streaming feeds that have multiple flows per second coming through.
     * All related flows need to join and point back to a single primary feed for job execution rollup
     */
    protected String primaryFlowFile;



    public BatchFeedStartingJobEventCacheEntry(String mapKey, DateTime firstEventTime, Long sampleTimeInMillis, Integer sampleEventRate){
       super(mapKey,firstEventTime,sampleTimeInMillis,sampleEventRate);
    }


    @Override
    protected void resetSampleEventCount(ProvenanceEventRecordDTO event) {
        super.resetSampleEventCount(event);
        if(startingFlowFileIds != null) {
            startingFlowFileIds.clear();
        }
        primaryFlowFile = null;
    }


    public boolean process(ProvenanceEventRecordDTO event) {
        boolean processed = super.process(event);
        if(processed) {
            //if it is a starting event then we need to record the flow file that is starting.
            //this will be used as a reference to other flows that are not processed (i.e. when multiple starting flows execute in a streaming fashion
            // those flows that are not processed will relate back to this flow
            if (event.isStartOfJob()) {
                //mark this event as the main feed flow file to track
                if (startingFlowFileIds == null) {
                    startingFlowFileIds = new HashSet<>();
                }
                startingFlowFileIds.add(event.getFlowFileUuid());
                if (primaryFlowFile == null) {
                    primaryFlowFile = event.getFlowFileUuid();
                    //event.getFeedFlowFile().setPrimaryRelatedBatchFeedFlow(event.getFeedFlowFile().getId());
                }
            }
        }else {
            if (event.isStartOfJob()) {
                //mark the entire feedflow as a stream if its the start of the job
                event.getFeedFlowFile().setStream(true);
                //relate the flows together so they are tracked under the same Job in Kylo Ops Manager
                event.getFeedFlowFile().setRelatedBatchFeedFlows(startingFlowFileIds);
                event.getFeedFlowFile().setPrimaryRelatedBatchFeedFlow(primaryFlowFile);

            }
        }
        return processed;
    }


}
