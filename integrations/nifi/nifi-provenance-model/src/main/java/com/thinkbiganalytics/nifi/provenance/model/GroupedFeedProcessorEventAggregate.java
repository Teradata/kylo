package com.thinkbiganalytics.nifi.provenance.model;

import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.ProvenanceEventStats;
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventUtil;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Determines a Stream by looking at the last {n} events and determines if the time between each of the events is within a given streaming threshold.
 *
 * If the event is detected as being a stream but the originating flow file event ( the one that started the job) was declared a Batch event then it will also process it
 *
 * Created by sr186054 on 8/25/16.
 */
public class GroupedFeedProcessorEventAggregate implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(GroupedFeedProcessorEventAggregate.class);

    private String feedName;
    private String processorId;
    private String processorName;
    private DateTime lastEventTime;
    private DateTime secondToLastEventTime;
    private DateTime lastSystemTime;
    private boolean isLastEventStream;


    private List<ProvenanceEventRecordDTO> potentialStreamEvents = new ArrayList<>();
    private Set<ProvenanceEventRecordDTO> jmsEvents = new HashSet<>();

    private List<ProvenanceEventRecordDTO> streamEvents = new ArrayList<>();
    private Map<String, ProvenanceEventRecordDTO> lastStreamEventByJob = new ConcurrentHashMap<>();


    private AggregatedProcessorStatistics stats;

    private Long allowedMillisBetweenEvents;

    private AtomicInteger eventCount = new AtomicInteger(0);
    private AtomicInteger batchCount = new AtomicInteger(0);
    private AtomicInteger streamingCount = new AtomicInteger(0);

    private AtomicInteger tempStreamingCount = new AtomicInteger(0);

    private DateTime initTime;

    private Integer numberOfEventsThatMakeAStream;

    private final ReentrantLock lock = new ReentrantLock(true);

    public GroupedFeedProcessorEventAggregate(String feedName, String processorId, Long allowedMillisBetweenEvents, Integer numberOfEventsThatMakeAStream) {

        this.feedName = feedName;
        this.processorId = processorId;
        this.allowedMillisBetweenEvents = allowedMillisBetweenEvents;
        this.numberOfEventsThatMakeAStream = numberOfEventsThatMakeAStream;
        this.stats = new AggregatedProcessorStatistics(processorId, feedName);
        this.initTime = DateTime.now();
        this.lastSystemTime = DateTime.now();
        log.info("new GroupedFeedProcessorEventAggregate for " + feedName + "," + processorId + " - " + this.initTime);
    }


    public GroupedFeedProcessorEventAggregate addEvent(ProvenanceEventRecordDTO event, ProvenanceEventStats stats) {
        groupEventAsStreamOrBatch(event, stats);
        lastSystemTime = DateTime.now();
        secondToLastEventTime = lastEventTime;
        lastEventTime = event.getEventTime();
        isLastEventStream = event.isStream();
        return this;
    }

    public boolean addRootFlowFileCompletionEvent(ProvenanceEventRecordDTO event) {
        if (!lastStreamEventByJob.containsKey(lastStreamEventMapKey(event))) {
            groupEventAsStreamOrBatch(event, true);
          //  log.info("adding completion event to queue for {} , {} ff: {}, rff: {}  ", event, event.getEventId(), event.getFlowFileUuid(), event.getJobFlowFileId());
            return true;
        }
        return false;
    }

    private String lastStreamEventMapKey(ProvenanceEventRecordDTO event) {
        return event.getJobFlowFileId() + "_" + event.getEventType() + "_" + event.isEndOfJob() + "_" + event.isStartOfJob() + "_" + event.getComponentId() + "_" + event.isFailure();
    }

    /**
     * Move an Event to the Batch list
     */
    private void moveToBatch(ProvenanceEventRecordDTO event) {
        event.setIsBatchJob(true);
        if (event.isStartOfJob()) {
            event.getFlowFile().getRootFlowFile().setFirstEventType(RootFlowFile.FIRST_EVENT_TYPE.BATCH);
        }
        lastStreamEventByJob.put(lastStreamEventMapKey(event), event);
        streamEvents.add(event);
        streamingCount.incrementAndGet();

    }

    private List<ProvenanceEventRecordDTO> movePotentialStreamToBatch() {
        List<ProvenanceEventRecordDTO> list = new ArrayList<>();
        if (!potentialStreamEvents.isEmpty()) {
            markFirstEventsAsBatch(potentialStreamEvents);
           list.addAll(Lists.newArrayList(potentialStreamEvents));
            jmsEvents.addAll(potentialStreamEvents);
            batchCount.addAndGet(potentialStreamEvents.size());
            potentialStreamEvents.clear();
        }
        return list;
    }


    private void movePotentialStreamToStream() {
        if (!potentialStreamEvents.isEmpty()) {
            potentialStreamEvents.stream().forEach(e -> {
                e.setStream(true);
                if (e.isStartOfJob()) {
                    e.getFlowFile().getRootFlowFile().setFirstEventType(RootFlowFile.FIRST_EVENT_TYPE.STREAM);
                    log.debug("Starting of job moved from potential to a stream for event {}", e);
                }

                lastStreamEventByJob.put(lastStreamEventMapKey(e), e);
              //  markStreamAsBatchForEventWithRelatedBatchJobs(e);
            });

            streamEvents.addAll(potentialStreamEvents);
            streamingCount.addAndGet(potentialStreamEvents.size());
            potentialStreamEvents.clear();
        }
    }

    private void moveToStream(ProvenanceEventRecordDTO event) {
        if(event.isStartOfJob()) {
            log.debug("Starting of job is a stream for event {}", event);
        }
        event.setStream(true);
        if (event.isStartOfJob()) {
            event.getFlowFile().getRootFlowFile().setFirstEventType(RootFlowFile.FIRST_EVENT_TYPE.STREAM);
        }
        lastStreamEventByJob.put(lastStreamEventMapKey(event), event);
     //   markStreamAsBatchForEventWithRelatedBatchJobs(event);
        streamEvents.add(event);
        streamingCount.incrementAndGet();

    }


    /**
     * This would handle a use case where a lot of files come in, and somehow 1 of those is sent off and runs as a batch
     *
     * @param event
     */
    private void markStreamAsBatchForEventWithRelatedBatchJobs(ProvenanceEventRecordDTO event) {
        if (event.getRelatedRootFlowFiles() != null) {
            if (event.getFlowFile().getRootFlowFile().getRelatedRootFlowFiles() != null) {
                event.getFlowFile().getRootFlowFile().getRelatedRootFlowFiles().forEach(ff -> {
                    if (ff.isBatch()) {

                        //if this ff is a stream, but the parent is a batch, ensure that all the parent events related to this ff are marked as a batch and added to be processed
                        if (event.getFlowFile().getRootFlowFile().isStream()) {
                            event.getFlowFile().getRootFlowFile().setFirstEventType(RootFlowFile.FIRST_EVENT_TYPE.BATCH);
                            Set<ProvenanceEventRecordDTO> eventsToAdd = new HashSet<ProvenanceEventRecordDTO>();
                            eventsToAdd.addAll(event.getFlowFile().getRootFlowFile().getCompletedEvents());
                            eventsToAdd.add(event);
                            eventsToAdd.forEach(e -> {
                                lastStreamEventByJob.put(lastStreamEventMapKey(e), e);
                            });
                            log.debug("adding {} jobs as batch and for event {} as BATCH Job and updating JobFlowFileId from {} to {} ", eventsToAdd.size(), event, event.getJobFlowFileId(),
                                      ff.getId());
                            event.setStream(false);
                            //mark as Batch job and reassing jobId to match that of the batch job?
                            // event.setJobFlowFileId(ff.getId());
                            event.setIsBatchJob(true);
                            //event.getFlowFile().getRootFlowFile().setFirstEvent(ff.getFirstEvent());
                        }

                        // event.setJobEventId(ff.getFirstEvent().getEventId());
                    }
                });
            }
        }
    }


    private void groupEventAsStreamOrBatch(ProvenanceEventRecordDTO event, ProvenanceEventStats stats) {

        groupEventAsStreamOrBatch(event, (stats.getJobsFinished() == 1L));
    }

    private void groupEventAsStreamOrBatch(ProvenanceEventRecordDTO event, boolean isRootFLowFileFinished) {
        lock.lock();
        try {

            if (lastEventTime == null) {
                lastEventTime = event.getEventTime();
            }


            if (ProvenanceEventUtil.isCompletionEvent(event)) {
                eventCount.incrementAndGet();
                checkAndMarkAsEndOfJob(event, isRootFLowFileFinished);
                //if the event is not the first event, but the first event is a Stream then move this to a stream
                if (RootFlowFile.FIRST_EVENT_TYPE.STREAM.equals(event.getFlowFile().getRootFlowFile().getFirstEventType())) {
                    moveToStream(event);

                } else {
                    //if the current event time is before the next allotted time for a batch event, then this is a potential stream, or if the lastEvent was indicated as a stream
                    if (event.getEventTime().isBefore(lastEventTime.plus(allowedMillisBetweenEvents)) || isLastEventStream) {
                        if (tempStreamingCount.incrementAndGet() >= numberOfEventsThatMakeAStream) {
                            movePotentialStreamToStream();
                            moveToStream(event);
                            //checkAndAddJobCompletionEvents(event,stats,streamEvents);
                        } else {
                            potentialStreamEvents.add(event);
                            // checkAndAddJobCompletionEvents(event,stats,potentialStreamEvents);

                        }
                    } else {
                        potentialStreamEvents.add(event);
                        //  checkAndAddJobCompletionEvents(event,stats,potentialStreamEvents);
                        /// no longer a stream event
                        if (event.isStartOfJob()) {
                            log.debug("Starting of job is a converted from stream to a batch for event {} ", event);
                        }

                        List<ProvenanceEventRecordDTO> movedEvents = movePotentialStreamToBatch();

                        tempStreamingCount.set(0);
                    }
                }
            } else {
                //    log.info("Non completion event {} ", event);
            }
        } finally {
            lock.unlock();
        }
    }



    private void checkAndMarkAsEndOfJob(ProvenanceEventRecordDTO event, ProvenanceEventStats stats) {
        checkAndMarkAsEndOfJob(event, stats.getJobsFinished() == 1L);
    }

    private void checkAndMarkAsEndOfJob(ProvenanceEventRecordDTO event, boolean jobFinished) {
        if (jobFinished && !event.getFlowFile().isRootFlowFile()) {
            //    log.info("Marking {} as the end of the job for {}.  is already end of job? {} ", event.getEventId(), event.getFlowFile().getRootFlowFile().getId(), event.isEndOfJob());
            event.setIsEndOfJob(true);
        }
    }

    private String eventsToString(Collection<ProvenanceEventRecordDTO> events) {
        return StringUtils.join(events.stream().map(e -> e.getEventId()).collect(Collectors.toList()), ",");
    }

    private List<ProvenanceEventRecordDTO> addStreamingEventsWhoseFirstEventWasABatchToQueue() {
        // each job does not need all events.  they just need the start and ending events in this batch relative to the jobflowfileid
        List<ProvenanceEventRecordDTO> events = lastStreamEventByJob.values().stream().filter(
            e -> RootFlowFile.FIRST_EVENT_TYPE.BATCH.equals(e.getFlowFile().getRootFlowFile().getFirstEventType())).collect(Collectors.toList());
        if (events != null && !events.isEmpty()) {
            jmsEvents.addAll(events);
        }
        return events;
    }

    private void markFirstEventsAsBatch(List<ProvenanceEventRecordDTO> events) {
        events.stream().filter(e -> e.isStartOfJob()).map(e -> e.getFlowFile().getRootFlowFile()).forEach(ff -> ff.setFirstEventType(RootFlowFile.FIRST_EVENT_TYPE.BATCH));
    }

    private void printList(List<ProvenanceEventRecordDTO> list, String title) {
        if(list != null && !list.isEmpty()) {
            log.info("Print {} - {} ", title, list.size());
            for (ProvenanceEventRecordDTO e : list) {
                log.info(" {} Event {}, {} - {}, isBatch? {} ", title, e.getEventId(), e.getComponentName(), e.getEventTime(), e.isBatchJob());
            }
        }
    }

    public List<ProvenanceEventRecordDTO> collectEventsToBeSentToJmsQueue() {
        lock.lock();

        List<ProvenanceEventRecordDTO> events = null;
        try {
            DateTime now = DateTime.now();

            //Move anything that is potential to batch if the time between is greater
            if (now.isAfter(lastEventTime.plus(allowedMillisBetweenEvents))) {
                if (secondToLastEventTime == null || (secondToLastEventTime != null && lastEventTime.isAfter(secondToLastEventTime.plus(allowedMillisBetweenEvents)))) {
                    //BATCH!
                    tempStreamingCount.set(0);
                    List<ProvenanceEventRecordDTO> movedEvents = movePotentialStreamToBatch();
                } else {
                    if (!potentialStreamEvents.isEmpty()) {
                        //treat as stream
                        potentialStreamEvents.forEach(e -> e.setStream(true));
                    }
                    potentialStreamEvents.clear();
                    tempStreamingCount.set(0);
                }

            }
            //if the First Event was a Batch event we should pass this event through so it gets reconciled in the Ops Manager
          List<ProvenanceEventRecordDTO> eventsAddedToBatch = addStreamingEventsWhoseFirstEventWasABatchToQueue();

            //mark as batch
            jmsEvents.stream().forEach(e -> {
                e.setIsBatchJob(true);
            });
           // printList(Lists.newArrayList(eventsAddedToBatch), " streaming events that were added to batch jms queue ");
            // printList(Lists.newArrayList(jmsEvents),"batch events in jms queue ");

            //copy and clear
            events = new ArrayList<>(jmsEvents);
            jmsEvents.clear();
            streamEvents.clear();
            lastStreamEventByJob.clear();
        } finally {
            lock.unlock();
        }

        return events == null ? new ArrayList<>() : events;

    }



    public void add(ProvenanceEventStats stats, ProvenanceEventRecordDTO event) {
        if (event.getComponentName() != null && processorName == null) {
            processorName = event.getComponentName();
        }
        addEvent(event, stats);
        addEventStats(stats);


    }

    private GroupedFeedProcessorEventAggregate addEventStats(ProvenanceEventStats stats) {
        if (stats != null) {
            this.stats.add(stats);
        }
        return this;
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


    public AggregatedProcessorStatistics getStats() {
        return stats;
    }

    public void setStats(AggregatedProcessorStatistics stats) {
        this.stats = stats;
    }


    public String getSummary() {
        return stats.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FeedProcessorEventAggregate{");
        sb.append("feedName='").append(feedName).append('\'');
        sb.append(", processorId='").append(processorId).append('\'');
        sb.append(", summary='").append(getSummary()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
