package com.thinkbiganalytics.nifi.provenance;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.provenance.cache.CacheUtil;
import com.thinkbiganalytics.nifi.provenance.jms.ProvenanceEventActiveMqWriter;
import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.BatchFeedProcessorEvents;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.ProvenanceEventStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.StatsModel;
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventUtil;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component
public class ProvenanceEventAggregator {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventAggregator.class);

    /**
     * Reference to when to send the aggregated events found in the  statsByTime map.
     */
    private DateTime lastCollectionTime = null;

    @Autowired
    private ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter;

    @Autowired
    ProvenanceFeedLookup provenanceFeedLookup;

    @Autowired
    ProvenanceStatsCalculator statsCalculator;

    @Autowired
    CacheUtil cacheUtil;

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

    /**
     * The Map of Objects that determine if the events for a Feed/processor are stream or batch
     */
    Map<String, BatchFeedProcessorEvents> eventsToAggregate = new ConcurrentHashMap<>();

    /**sometimes events come in before their root flow file.  Root Flow files are needed for processing.
     *if the events come in out of order, queue them in this map
     *@see #processEarlyChildren method
     **/
    Cache<String, ConcurrentLinkedQueue<ProvenanceEventRecordDTO>> jobFlowFileIdEarlyChildrenMap = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    @Autowired
    public ProvenanceEventAggregator(@Qualifier("provenanceEventActiveMqWriter") ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter) {
        super();

        log.debug("************** NEW ProvenanceEventAggregator  activemq: {} ", provenanceEventActiveMqWriter);
        this.provenanceEventActiveMqWriter = provenanceEventActiveMqWriter;
        this.lastCollectionTime = DateTime.now();


    }


    private String mapKey(ProvenanceEventRecordDTO event) {
        return event.getFeedName() + ":" + event.getComponentId();
    }

    private void processEarlyChildren(String jobFlowFileId) {
        //if this is the start of the job the check and determine if there are any children that were initialized for this flowfile before receiving the start event and then reprocess them
        ConcurrentLinkedQueue<ProvenanceEventRecordDTO> queue = jobFlowFileIdEarlyChildrenMap.getIfPresent(jobFlowFileId);
        if (queue != null) {
            ProvenanceEventRecordDTO nextEvent = null;
            while ((nextEvent = queue.poll()) != null) {
                log.debug("Processing early child {} since the root flowfile {} has been processed.", nextEvent, jobFlowFileId);
                process(nextEvent);
            }
            jobFlowFileIdEarlyChildrenMap.invalidate(jobFlowFileId);
        }
    }



    public void process(ProvenanceEventRecordDTO event) {
        try {
            if (event != null) {
                try {

                    cacheUtil.cacheAndBuildFlowFileGraph(event);
                    // log.info("Process Event {} ", event);
                    if (ProvenanceEventUtil.isDropFlowFilesEvent(event)) {
                        // a Drop event component id will be the connection, not the processor id. we will set the name of the component
                        event.setComponentName("FlowFile Queue emptied");
                        event.setIsFailure(true);
                        event.getFlowFile().addFailedEvent(event);
                    }
                    //send the event off for stats processing
                    ProvenanceEventStats stats = statsCalculator.calculateStats(event);
                    //check to see if the event is going through a processor deemed as a failure in kylo
                    collectFailureEvents(event);

                        // check to see if the parents should be finished
                        if (event.isEndingFlowFileEvent()) {
                            Set<ProvenanceEventRecordDTO> completedRootFlowFileEvents = completeStatsForParentFlowFiles(event);
                            if (completedRootFlowFileEvents != null) {
                                completedRootFlowFileEvents(completedRootFlowFileEvents);
                            }

                            if (event.isEndOfJob() && event.getFlowFile().getRootFlowFile().isFlowAndRelatedRootFlowFilesComplete()) {
                                //trigger Complete event for event and flowfile
                                collectCompletionEvents(event);
                            }
                        }
                    if (!event.isStream()) {
                        boolean added = aggregateEvent(event);
                    }

                        //might not need this!!
                        if (event.isStartOfJob()) {
                            processEarlyChildren(event.getJobFlowFileId());
                        }

                } catch (RootFlowFileNotFoundException e) {
                    //add to a holding bin
                    log.debug("Holding on to {} since the root flow file {} has yet to be processed.", event, event.getFlowFileUuid());
                    jobFlowFileIdEarlyChildrenMap.asMap().computeIfAbsent(event.getFlowFileUuid(), (flowFileId) -> new ConcurrentLinkedQueue<ProvenanceEventRecordDTO>()).add(event);

                }
                log.trace("Processed Event {} ", event);
            }
        } catch (Exception e) {
            log.error("ERROR PROCESSING EVENT! {}.  ERROR: {} ", event, e.getMessage(), e);
        }
    }


    /**
     * get list of EventStats marking the flowfile as complete for the direct parent flowfiles if the child is complete and the parent is complete
     */
    public Set<ProvenanceEventRecordDTO> completeStatsForParentFlowFiles(ProvenanceEventRecordDTO event) {

        ActiveFlowFile rootFlowFile = event.getFlowFile().getRootFlowFile();
        //  log.info("try to complete parents for {}, root: {},  parents: {} ", event.getFlowFile().getId(), rootFlowFile.getId(), event.getFlowFile().getParents().size());

        if (event.isEndingFlowFileEvent() && event.getFlowFile().hasParents()) {
            log.debug("Attempt to complete all {} parent Job flow files that are complete", event.getFlowFile().getParents().size());
            List<ProvenanceEventStats> list = new ArrayList<>();
            Set<ProvenanceEventRecordDTO> eventList = new HashSet<>();
            event.getFlowFile().getParents().stream().filter(parent -> parent.isCurrentFlowFileComplete()).forEach(parent -> {
                ProvenanceEventRecordDTO lastFlowFileEvent = parent.getLastEvent();
                ProvenanceEventStats stats = StatsModel.newJobCompletionProvenanceEventStats(event.getFeedName(), lastFlowFileEvent);
                if (stats != null) {
                    list.add(stats);
                    eventList.add(lastFlowFileEvent);

                }
            });
            if (!list.isEmpty()) {
                statsCalculator.addStats(list);
            }
            return eventList;
        }
        return null;

    }


    private void completedRootFlowFileEvents(Set<ProvenanceEventRecordDTO> completedEvents) {
        for (ProvenanceEventRecordDTO event : completedEvents) {

            if (event != null) {
                eventsToAggregate.computeIfAbsent(mapKey(event), mapKey -> new BatchFeedProcessorEvents(event.getFeedName(),
                                                                                                                  event
                                                                                                                      .getComponentId(), getMaxBatchFeedJobEventsPerSecond())).setMaxEventsPerSecond(
                    getMaxBatchFeedJobEventsPerSecond()).add(
                    event);
            }


        }
    }

    /**
     * send off all failures to ops mgr for recording
     * @param event
     */
    public void collectFailureEvents(ProvenanceEventRecordDTO event) {
        if (!event.isFailure() && provenanceFeedLookup.isFailureEvent(event)) {

            event.setIsFailure(true);
            event.getFlowFile().getRootFlowFile().addFailedEvent(event);
            ProvenanceEventStats stats = StatsModel.toFailureProvenanceEventStats(event.getFeedName(), event);
            statsCalculator.addFailureStats(stats);

        }
    }


    /**
     * Send off all final completion job events to ops manager for recording
     * @param event
     */
    private void collectCompletionEvents(ProvenanceEventRecordDTO event) {
        if (event.isEndOfJob()) {
            log.debug("collectCompletion {}  - {} - {} ", event.getJobFlowFileId(), event.getFlowFile().getRootFlowFile().getFirstEventType(), event.getFlowFile().getRootFlowFile().isBatch());
            if (event.getFlowFile() != null && event.getFlowFile().getRootFlowFile() != null) {
                log.debug("Setting event {} as batch? {} ", event, event.getFlowFile().getRootFlowFile().isBatch());
                event.setIsBatchJob(event.getFlowFile().getRootFlowFile().isBatch());
            }

            //  if(event.getFlowFile().getRootFlowFile().isFlowComplete() && event.getFlowFile().getRootFlowFile().areRelatedRootFlowFilesComplete()) {
            event.setIsFinalJobEvent(true);
            if (event.getFlowFile().getRootFlowFile().hasAnyFailures()) {
                event.setIsFailure(true);
                event.setHasFailedEvents(true);
            }
        }
    }

    /**
     *  grouped by Feed and Processor
     */
    private boolean aggregateEvent(ProvenanceEventRecordDTO event) {
        if (event != null) {
            return eventsToAggregate.computeIfAbsent(mapKey(event), mapKey -> new BatchFeedProcessorEvents(event.getFeedName(),
                                                                                                           event
                                                                                                               .getComponentId(), getMaxBatchFeedJobEventsPerSecond())).setMaxEventsPerSecond(
                getMaxBatchFeedJobEventsPerSecond()).add(event);
        }
        return false;
    }


    public void sendToJms() {
        //update the collection time
        List<ProvenanceEventRecordDTO> eventsSentToJms = eventsToAggregate.values().stream()
            .flatMap(feedProcessorEventAggregate -> feedProcessorEventAggregate.collectEventsToBeSentToJmsQueue().stream())
            .collect(Collectors.toList());
        sendBatchFeedEvents(eventsSentToJms);
        statsCalculator.sendStats();
        lastCollectionTime = DateTime.now();

    }

    private void sendBatchFeedEvents(List<ProvenanceEventRecordDTO> elements) {
        if (elements != null && !elements.isEmpty()) {
            log.debug("processQueue for {} Nifi Events ", elements.size());
            Lists.partition(elements, getJmsEventGroupSize()).forEach(eventsSubList -> {
                ProvenanceEventRecordDTOHolder eventRecordDTOHolder = new ProvenanceEventRecordDTOHolder();
                eventRecordDTOHolder.setEvents(Lists.newArrayList(eventsSubList));
                provenanceEventActiveMqWriter.writeBatchEvents(eventRecordDTOHolder);
            });
        }
    }

    public Integer getMaxBatchFeedJobEventsPerSecond() {
        return maxBatchFeedJobEventsPerSecond;
    }

    public void setMaxBatchFeedJobEventsPerSecond(Integer maxBatchFeedJobEventsPerSecond) {
        this.maxBatchFeedJobEventsPerSecond = maxBatchFeedJobEventsPerSecond;
    }

    public Integer getJmsEventGroupSize() {
        return jmsEventGroupSize == null ? 50 : jmsEventGroupSize;
    }

    public void setJmsEventGroupSize(Integer jmsEventGroupSize) {
        this.jmsEventGroupSize = jmsEventGroupSize;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProvenanceEventAggregator{");
        sb.append(", provenanceEventActiveMqWriter=").append(provenanceEventActiveMqWriter);
        sb.append(", lastCollectionTime=").append(lastCollectionTime);
        sb.append('}');
        return sb.toString();
    }


}
