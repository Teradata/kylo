package com.thinkbiganalytics.nifi.provenance.model;

/*-
 * #%L
 * thinkbig-nifi-provenance-model
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provenance events for Feeds not marked as "streaming" will be processed by this class in the KyloReportingTask of NiFi
 */
public class BatchFeedProcessorEvents implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(BatchFeedProcessorEvents.class);

    /**
     * The name of the feed.  Derived from the process group {category}.{feed}
     */
    private String feedName;


    private String processorName;

    /**
     * The Processor Id
     */
    private String processorId;

    /**
     * The time for the Last Event that has been processed
     */
    private DateTime lastEventTime;


    /**
     * the last time the events were sent to JMS
     **/
    private DateTime lastCollectionTime;

    /**
     * Collection of events that will be sent to jms
     */
    private Set<ProvenanceEventRecordDTO> jmsEvents = new LinkedHashSet<>();

    /**
     * Map to determine if the events coming in are rapid fire.  if so they wil be suppressed based upon the supplied {@code maxEventsPerSecond} allowed
     */
    Map<DateTime, Set<ProvenanceEventRecordDTO>> startingJobEventsBySecond = new HashMap<>();

    /**
     * The max number of starting job events for the given feed and processor allowed to pass through per second This parameter is passed in via the constructor
     */
    private Integer maxEventsPerSecond = 10;


    /**
     * Time when this group first got created
     */
    private DateTime initTime;


    public BatchFeedProcessorEvents(String feedName, String processorId, Integer maxEventsPerSecond) {

        this.feedName = feedName;
        this.processorId = processorId;
        this.initTime = DateTime.now();
        this.maxEventsPerSecond = maxEventsPerSecond;
        log.debug("new BatchFeedProcessorEvents for " + feedName + "," + processorId + " - " + this.initTime);
    }


    /**
     * Add the event to be processed
     * @param event the event to add to the batch
     * @return true if added, false if not
     */
    public boolean add(ProvenanceEventRecordDTO event) {
        if (event.getComponentName() != null && processorName == null) {
            processorName = event.getComponentName();
        }
        return addEvent(event);


    }

    /**
     * Check to see if we are getting events too fast to be considered a batch.  If so suppress the events so just a few go through and the rest generate statistics.
     *
     * @param event the event to check
     * @return true if suppressing the event (not adding it to the batch of events), false if it will be added
     */
    private boolean isSuppressEvent(ProvenanceEventRecordDTO event) {
        if (event.isStream() || event.getFeedFlowFile().isStream()) {
            event.setStream(true);
            log.warn(" Event {} has been suppressed from Kylo Ops Manager. Its parent starting event was detected as a stream for feed {} and processor: {} ", event, maxEventsPerSecond, feedName,
                     processorName);
            return true;
        } else if (event.isStartOfJob()) {
            DateTime time = event.getEventTime().withMillisOfSecond(0);
            startingJobEventsBySecond.computeIfAbsent(time, key -> new HashSet<ProvenanceEventRecordDTO>()).add(event);
            if (startingJobEventsBySecond.get(time).size() > maxEventsPerSecond) {
                event.getFeedFlowFile().setStream(true);
                event.setStream(true);
                log.warn(" Event  {} has been suppressed from Kylo Ops Manager.  more than {} events per second were detected for feed {} and processor: {} ", event, maxEventsPerSecond, feedName,
                         processorName);
                return true;
            }
        }
        return false;
    }


    /**
     * Add an event from Nifi to be processed
     * @param event the event to add for batch processing
     * @return returns true if successfully added, false if not.  It may return false if the event is suppressed
     *
     * @see this#isSuppressEvent(ProvenanceEventRecordDTO)
     */
    public boolean addEvent(ProvenanceEventRecordDTO event) {
        if (!isSuppressEvent(event)) {

            if (lastEventTime == null) {
                lastEventTime = event.getEventTime();
            }

            event.setIsBatchJob(true);
            jmsEvents.add(event);

            lastEventTime = event.getEventTime();
            return true;
        }
        return false;
    }


    /**
     * for all the events that have been processed, send them off to the JMS queue
     * @return the list of events that have been sent
     */
    public List<ProvenanceEventRecordDTO> collectEventsToBeSentToJmsQueue() {
        List<ProvenanceEventRecordDTO> events = null;
        try {
            events = new ArrayList<>(jmsEvents);
            jmsEvents.clear();
        } finally {

        }
        lastCollectionTime = DateTime.now();
        startingJobEventsBySecond.clear();
        return events == null ? Collections.emptyList() : events;
    }


    /**
     * get the feed name for the batch of events
     * @return the name of the feed
     */
    public String getFeedName() {
        return feedName;
    }

    /**
     * Sets the feed name
     * @param feedName the name of the feed
     */
    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    /**
     * Gets the processorId
     * @return the processor Id
     */
    public String getProcessorId() {
        return processorId;
    }

    /**
     * Sets the processor id
     * @param processorId the processor id
     */
    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    /**
     *
     * @return the display name for the processor
     */
    public String getProcessorName() {
        return processorName;
    }

    /**
     *
     * @param processorName the name of the processor
     */
    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    /**
     *
     * @param maxEventsPerSecond the max number of events allowed per sec to be considered a batch job
     * @return this class
     */
    public BatchFeedProcessorEvents setMaxEventsPerSecond(Integer maxEventsPerSecond) {
        this.maxEventsPerSecond = maxEventsPerSecond;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BatchFeedProcessorEventAggregate{");
        sb.append("feedName='").append(feedName).append('\'');
        sb.append(", processorId='").append(processorId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
