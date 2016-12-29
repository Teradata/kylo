package com.thinkbiganalytics.nifi.provenance.model;

import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventUtil;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
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
     */
    public boolean add(ProvenanceEventRecordDTO event) {
        if (event.getComponentName() != null && processorName == null) {
            processorName = event.getComponentName();
        }
        return addEvent(event);


    }

    private boolean isSuppressEvent(ProvenanceEventRecordDTO event) {
        if (event.isStream() || event.getFlowFile().getRootFlowFile().isStream()) {
            event.setStream(true);
            log.warn(" EVENT {} WAS SUPPRESSED because its parent starting event was detected as a stream for feed {} and processor: {} ", event, maxEventsPerSecond, feedName, processorName);
            return true;
        } else if (event.isStartOfJob()) {
            DateTime time = event.getEventTime().withMillisOfSecond(0);
            startingJobEventsBySecond.computeIfAbsent(time, key -> new HashSet<ProvenanceEventRecordDTO>()).add(event);
            if (startingJobEventsBySecond.get(time).size() > maxEventsPerSecond) {
                event.getFlowFile().getRootFlowFile().markAsStream();
                event.setStream(true);
                log.warn(" EVENT  {} WAS SUPPRESSED FROM Kylo Ops Manager.  more than {} events per second were detected for feed {} and processor: {} ", event, maxEventsPerSecond, feedName,
                         processorName);
                return true;
            }
        }
        return false;
    }

    private String mapKey(ProvenanceEventRecordDTO eventRecordDTO) {
        return eventRecordDTO.getJobFlowFileId();
    }

    /**
     * Add an event from Nifi to be processed
     */
    public boolean addEvent(ProvenanceEventRecordDTO event) {
        if (!isSuppressEvent(event)) {

            if (lastEventTime == null) {
                lastEventTime = event.getEventTime();
            }

            if (ProvenanceEventUtil.isCompletionEvent(event)) {
                checkAndMarkAsEndOfJob(event, event.getFlowFile().getRootFlowFile().isFlowComplete());
                log.info("BATCH Adding Batch event {} ", event);
                event.setIsBatchJob(true);
                event.getFlowFile().getRootFlowFile().markAsBatch();
                if (event.getPreviousEvent() == null && !event.isStartOfJob()) {
                    event.getFlowFile().setPreviousEvent(event);
                }
                jmsEvents.add(event);
            }

            lastEventTime = event.getEventTime();
            return true;
        }
        return false;
    }


    /**
     * Sets the flag on the event if this event is really the ending of the RootFlowFile
     */
    private void checkAndMarkAsEndOfJob(ProvenanceEventRecordDTO event, boolean jobFinished) {
        if (jobFinished && !event.getFlowFile().isRootFlowFile()) {
            event.setIsEndOfJob(true);
        }
    }


    public List<ProvenanceEventRecordDTO> collectEventsToBeSentToJmsQueue() {
        List<ProvenanceEventRecordDTO> events = null;
        try {
            events = new ArrayList<>(jmsEvents);
            jmsEvents.clear();
        } finally {

        }
        lastCollectionTime = DateTime.now();
        startingJobEventsBySecond.clear();
        return events == null ? new ArrayList<>() : events;

    }


    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

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
