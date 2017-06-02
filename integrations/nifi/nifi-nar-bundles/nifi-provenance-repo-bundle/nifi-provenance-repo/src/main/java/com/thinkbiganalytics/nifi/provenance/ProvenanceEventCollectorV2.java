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

import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.provenance.cache.FeedFlowFileCacheListener;
import com.thinkbiganalytics.nifi.provenance.cache.FeedFlowFileCacheUtil;
import com.thinkbiganalytics.nifi.provenance.jms.ProvenanceEventActiveMqWriter;
import com.thinkbiganalytics.nifi.provenance.repo.BatchFeedProcessorEventsV2;
import com.thinkbiganalytics.nifi.provenance.model.FeedFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Process a Kylo managed ProvenanceEvent. If indicated as a Stream ({@link ProvenanceEventRecordDTO#isStream()}) the system will just generate Statistics {@link
 * com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatistics} grouping the events by Feed and Processor Id Otherwise if not indicated a stream, it will be processed as a
 * Batch job and send the full Event to JMS
 */
public class ProvenanceEventCollectorV2  {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventCollectorV2.class);
    @Autowired
    ProvenanceStatsCalculator statsCalculator;
    @Autowired
    FeedFlowFileCacheUtil cacheUtil;
    /**
     * The Map of Objects that will be grouped and sent over to Kylo as Batch Jobs/Steps for Operations Manager
     */
    Map<String, BatchFeedProcessorEventsV2> groupedBatchEventsByFeed = new ConcurrentHashMap<>();
    @Autowired
    private ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter;
    /**
     * Safeguard against the system sending too many batch feed events through to Kylo
     * This is the  max events per second allowed for a feed/processor combo
     * if a given batch exceeds this threshold the remaining jobs will be suppressed
     * All jobs will calculate statistics about the feeds
     */
    private Integer maxBatchFeedJobEventsPerSecond = 10;
    /**
     * Size of the group of events that will be batched and sent to Kylo
     */
    private Integer jmsEventGroupSize = 50;


    @Autowired
    public ProvenanceEventCollectorV2(@Qualifier("provenanceEventActiveMqWriter") ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter) {
        super();

        this.provenanceEventActiveMqWriter = provenanceEventActiveMqWriter;
    }


    /**
     * The key to use to batch up the events by Feed and Processor.
     *
     * @param event the event to process
     * @return the key based upon the feed name and the component id
     */
    private String mapKey(ProvenanceEventRecordDTO event) {
        return event.getFeedFlowFile().getFirstEventProcessorId() + ":" + event.getComponentId();
    }


    /**
     * determine if the event has Feed
     *
     * @param event the event to check
     * @return true if feed name is set, false if not
     */
    private boolean hasFeedName(ProvenanceEventRecordDTO event) {
        return StringUtils.isNotBlank(event.getFeedName());
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
                    //only process if we can get the feed name, otherwise its no use
                //    if (hasFeedName(event)) {
                        //send the event off for stats processing
                        statsCalculator.calculateStats(event);

                        //batch up the data to send to kylo if this feed is marked as a batch or if the parent flow file is marked as a batch
                       // if (!event.isStream()) {
                            boolean added = batchEvent(event);

                       // }

                  //  } else {
                  //      log.error("Provenance: Cant find Feed for {} ", event);
                   // }

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
        if (event != null) {
            return groupedBatchEventsByFeed.computeIfAbsent(mapKey(event), mapKey -> new BatchFeedProcessorEventsV2(event.getJobFlowFileId(),event.getComponentId(), getMaxBatchFeedJobEventsPerSecond()).setMaxEventsPerSecond(getMaxBatchFeedJobEventsPerSecond())).add(event);
        }
        return false;
    }


    /**
     * Send both the Statistics {@link com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder} and the Batched Provenance Events {@link
     * ProvenanceEventRecordDTOHolder } to JMS for Kylo Operations Manager to process
     */
    public void sendToJms() {
        //update the collection time
        List<ProvenanceEventRecordDTO> eventsSentToJms = groupedBatchEventsByFeed.values().stream()
            .flatMap(feedProcessorEventAggregate -> feedProcessorEventAggregate.collectEventsToBeSentToJmsQueue().stream())
            .collect(Collectors.toList());
        sendBatchFeedEvents(eventsSentToJms);
        statsCalculator.sendStats();
    }

    /**
     * Send the Batched events over to JMS
     *
     * @param elements The events to send to JMS
     */
    private void sendBatchFeedEvents(List<ProvenanceEventRecordDTO> elements) {
        if (elements != null && !elements.isEmpty()) {
            Lists.partition(elements, getJmsEventGroupSize()).forEach(eventsSubList -> {
                ProvenanceEventRecordDTOHolder eventRecordDTOHolder = new ProvenanceEventRecordDTOHolder();
                eventRecordDTOHolder.setEvents(Lists.newArrayList(eventsSubList));
                provenanceEventActiveMqWriter.writeBatchEvents(eventRecordDTOHolder);
            });
        }
    }

    /**
     * The Max number of events allowed per feed per second. This is passed in from the {@link com.thinkbiganalytics.nifi.provenance.reporting.KyloProvenanceEventReportingTask} configuration and used
     * to safeguard against processing too many Jobs/Event records as Batch Jobs.
     *
     * @return the maximum number of jobs/sec allowed for this feed to be considered a batch job
     */
    public Integer getMaxBatchFeedJobEventsPerSecond() {
        return maxBatchFeedJobEventsPerSecond;
    }

    public void setMaxBatchFeedJobEventsPerSecond(Integer maxBatchFeedJobEventsPerSecond) {
        this.maxBatchFeedJobEventsPerSecond = maxBatchFeedJobEventsPerSecond;
    }

    /**
     * Returns the sub group size of events to group together before sending to JMS.
     */
    public Integer getJmsEventGroupSize() {
        return jmsEventGroupSize == null ? 50 : jmsEventGroupSize;
    }

    public void setJmsEventGroupSize(Integer jmsEventGroupSize) {
        this.jmsEventGroupSize = jmsEventGroupSize;
    }



}
