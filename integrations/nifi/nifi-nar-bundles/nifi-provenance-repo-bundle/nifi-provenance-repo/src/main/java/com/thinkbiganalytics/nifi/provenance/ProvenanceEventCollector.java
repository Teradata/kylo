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

import com.thinkbiganalytics.nifi.provenance.cache.FeedFlowFileCacheUtil;
import com.thinkbiganalytics.nifi.provenance.jms.ProvenanceEventActiveMqWriter;
import com.thinkbiganalytics.nifi.provenance.model.FeedFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


/**
 * Process a Kylo managed ProvenanceEvent. If indicated as a Stream ({@link ProvenanceEventRecordDTO#isStream()}) the system will just generate Statistics {@link
 * com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatistics} grouping the events by Feed and Processor Id Otherwise if not indicated a stream, it will be processed as a
 * Batch job and send the full Event to JMS
 */
public class ProvenanceEventCollector {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventCollector.class);
    @Autowired
    ProvenanceStatsCalculator statsCalculator;
    @Autowired
    FeedFlowFileCacheUtil cacheUtil;

    @Autowired
    BatchProvenanceEvents batchProvenanceEvents;


    @Autowired
    public ProvenanceEventCollector() {
        super();
    }
   /**
     * Process the event, adding it to the running {@link FeedFlowFile} , calculating statistics on the event, and if a Batch feed, grouped by Feed and
     * Processor, process the entire event for processing.
     *
     * @param event the event to process
     */
    public void process(ProvenanceEventRecordDTO event) {
        try {
            if (event != null) {
                try {

                    cacheUtil.cacheAndBuildFlowFileGraph(event);
                    //if the Flow gets an "Empty Queue" message it means a user emptied the queue that was stuck in a connection.
                    // this means the flow cannot complete and will be treated as a failed flow and failed job
                    if (ProvenanceEventUtil.isFlowFileQueueEmptied(event)) {
                        // a Drop event component id will be the connection, not the processor id. we will set the name of the component
                        event.setComponentName("FlowFile Queue emptied");
                        event.setIsFailure(true);
                        event.setHasFailedEvents(true);
                        FeedFlowFile feedFlowFile = event.getFeedFlowFile();
                        if (feedFlowFile != null) {
                            feedFlowFile.checkAndMarkComplete(event);
                        }
                        event.getFeedFlowFile().incrementFailedEvents();
                    }
                        //send the event off for stats processing
                       statsCalculator.calculateStats(event);
                       batchEvent(event);
                       if(  event.isFinalJobEvent()){
                           cacheUtil.completeFlowFile(event.getFeedFlowFile());
                       }
                } catch (FeedFlowFileNotFoundException e) {
                    log.debug("Unable to find Root flowfile.", event, event.getFlowFileUuid());
                }
            }
        } catch (Exception e) {
            log.error("ERROR PROCESSING EVENT! {}.  ERROR: {} ", event, e.getMessage(), e);
        }
    }


    /**
     * Group the Event by Feed and then by Processor
     *
     * @param event the event to process
     * @return true if added, false if suppressed
     */
    private boolean batchEvent(ProvenanceEventRecordDTO event) {
       return batchProvenanceEvents.process(event);
    }


    /**
     * Send both the Statistics {@link com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder} and the Batched Provenance Events {@link
     * ProvenanceEventRecordDTOHolder } to JMS for Kylo Operations Manager to process
     */
    public void sendToJms() {
        batchProvenanceEvents.sendToJms();
        statsCalculator.sendStats();
    }





}
