package com.thinkbiganalytics.nifi.provenance.processor;

import com.google.common.collect.Iterables;
import com.thinkbiganalytics.nifi.provenance.StreamConfiguration;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.util.ProvenanceEventUtil;
import com.thinkbiganalytics.nifi.provenance.v2.cache.feed.ProvenanceFeedStatsCalculator;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Process Collection of Events and determine if they are Batch or Stream based upon the supplied StreamConfiguration Created by sr186054 on 8/13/16.
 */
public abstract class AbstractProvenanceEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(AbstractProvenanceEventProcessor.class);

    private StreamConfiguration streamConfiguration;

    private Map<String, List<ProvenanceEventRecordDTO>> processorProvenanceEvents = new HashMap<>();

    private Map<String, List<ProvenanceEventRecordDTO>> batchProvenanceEvents = new HashMap<>();

    private Map<String, List<ProvenanceEventRecordDTO>> potentialStreamingProcessors = new HashMap<>();

    private Map<String, List<ProvenanceEventRecordDTO>> streamingProcessors = new HashMap<>();


    public AbstractProvenanceEventProcessor(StreamConfiguration streamConfiguration) {
        this.streamConfiguration = streamConfiguration;
    }

    /**
     * Set of the #streamingMapKey which have already be designated as a Streaming Used for quick calculation if the Events coming in should be marked as a Stream or not
     */
    private Set<String> streamingProcessorKeys = new HashSet<>();


    private enum PROCESSING_TYPE {
        STREAM, POTENTIAL_STREAM, BATCH, UNKNOWN
    }


    private DateTime getLastEventProcessTime(ProvenanceEventRecordDTO event) {
        List<ProvenanceEventRecordDTO> events = processorProvenanceEvents.get(streamingMapKey(event));
        if (events != null) {
            ProvenanceEventRecordDTO lastEvent = Iterables.getLast(events, null);
            DateTime eventTime = new DateTime(lastEvent.getEventTime());
            return eventTime;
        }
        return null;
    }

    private void addToCollection(Map<String, List<ProvenanceEventRecordDTO>> map, ProvenanceEventRecordDTO event) {
        String key = streamingMapKey(event);
        if (map.get(key) == null) {
            map.put(key, new ArrayList<>());
        }
        map.get(key).add(event);
    }

    /**
     * gets the time from this event compared to the last event time that was processed for this proessor
     */
    private Long getTimeSinceLastEventForProcessor(ProvenanceEventRecordDTO event) {
        DateTime lastProcessTime = getLastEventProcessTime(event);
        if (lastProcessTime != null) {
            long diff = new DateTime(event.getEventTime()).getMillis() - lastProcessTime.getMillis();
            return diff;
        }
        return null;
    }

    /**
     * Return a KEY to determine if this event partakes in a STREAM or a BATCH
     */
    public abstract String streamingMapKey(ProvenanceEventRecordDTO event);

    private void moveToStream(Map<String, List<ProvenanceEventRecordDTO>> map, String key) {
        streamingProcessorKeys.add(key);
        map.get(key).stream().collect(Collectors.toList()).forEach(event -> addToCollection(streamingProcessors, event));
        map.remove(key);

    }

    private void moveToBatch(Map<String, List<ProvenanceEventRecordDTO>> map) {
        //take all elements in potential collection and move them to  batch
        map.values().stream().flatMap(events -> events.stream()).collect(Collectors.toList()).forEach(event -> addToCollection(batchProvenanceEvents, event));
    }

    public void process(List<ProvenanceEventRecordDTO> events) {

        if (events != null && !events.isEmpty()) {
            log.info("process {} events", events.size());
            //sort by time
            events.sort(ProvenanceEventUtil.provenanceEventRecordDTOComparator());
            //Process Each event as either Batch or Streaming
            events.forEach(event -> processEvent(event));
            //after processing if potential still has data then mark them as batch and clear
            //join the 2 collections and move to batch
            Map<String, List<ProvenanceEventRecordDTO>> joinedCollection = new HashMap<>(processorProvenanceEvents);

            if (!potentialStreamingProcessors.isEmpty()) {
                potentialStreamingProcessors.entrySet().forEach(entry ->
                                                                {
                                                                    if (joinedCollection.containsKey(entry.getKey())) {
                                                                        Set<ProvenanceEventRecordDTO> records = new HashSet<ProvenanceEventRecordDTO>(joinedCollection.get(entry.getKey()));
                                                                        records.addAll(entry.getValue());
                                                                        joinedCollection.put(entry.getKey(), new ArrayList<>(records));
                                                                    } else {
                                                                        joinedCollection.put(entry.getKey(), entry.getValue());
                                                                    }
                                                                });
            }

            moveToBatch(joinedCollection);
            //clear potential
            potentialStreamingProcessors.clear();
            processorProvenanceEvents.clear();
            //now streamingCollection and Batch collection should be populated correctly
            //update flow file stats


            processBatch();
            processStream();

        }

    }


    /**
     * Based upon the supplied configuration determine if this event is a Stream or a Batch
     */
    private PROCESSING_TYPE processEvent(ProvenanceEventRecordDTO event) {
        PROCESSING_TYPE processingType = PROCESSING_TYPE.UNKNOWN;
        String key = streamingMapKey(event);
        if (streamingProcessorKeys.contains(key)) {
            //mark as Stream
            addToCollection(streamingProcessors, event);
            processingType = PROCESSING_TYPE.STREAM;
        } else if (processorProvenanceEvents.containsKey(key)) {
            Long timeDiff = getTimeSinceLastEventForProcessor(event);
            if (timeDiff <= streamConfiguration.getMaxTimeBetweenEventsMillis()) {
                if (potentialStreamingProcessors.containsKey(key)) {
                    Integer size = potentialStreamingProcessors.get(key).size();
                    if (size >= streamConfiguration.getNumberOfEventsToConsiderAStream()) {
                        //this is a Stream.
                        // Move all potential stream events to streaming collection for this processor
                        //copy and move
                        moveToStream(potentialStreamingProcessors, key);
                        moveToStream(processorProvenanceEvents, key);
                        processingType = PROCESSING_TYPE.STREAM;
                    } else {
                        addToCollection(potentialStreamingProcessors, event);
                        processingType = PROCESSING_TYPE.POTENTIAL_STREAM;
                    }
                } else {
                    addToCollection(potentialStreamingProcessors, event);
                    processingType = PROCESSING_TYPE.POTENTIAL_STREAM;
                }
            } else {
                addToCollection(potentialStreamingProcessors, event);
                processingType = PROCESSING_TYPE.POTENTIAL_STREAM;
            }
        } else {
            //add it to processing Map
            addToCollection(processorProvenanceEvents, event);
        }
        return processingType;
    }


    private void processBatch() {
        //handle batch event
        log.info("Processing BATCH ");
        if (batchProvenanceEvents != null && !batchProvenanceEvents.isEmpty()) {

            batchProvenanceEvents.values().stream().flatMap(events -> events.stream()).sorted(ProvenanceEventUtil.provenanceEventRecordDTOComparator()).collect(Collectors.toList()).forEach(event -> {
                //what do to with batch
                log.info("Processing BATCH Event {}, {} ({}), for flowfile: {}  ", event.getEventId(), event.getDetails(), event.getComponentId(), event.getFlowFileUuid());
                ProvenanceFeedStatsCalculator.instance().calculateStats(event);
            });

        }
        batchProvenanceEvents.clear();

    }

    private void processStream() {
        //handle stream event
        log.info("Processing STREAM ");

        if (streamingProcessors != null && !streamingProcessors.isEmpty()) {

            streamingProcessors.values().stream().flatMap(events -> events.stream()).sorted(ProvenanceEventUtil.provenanceEventRecordDTOComparator()).collect(Collectors.toList()).forEach(event -> {
                //what do to with stream
                log.info("Processing STREAM Event {}, {} ({}), for flowfile: {}  ", event.getEventId(), event.getDetails(), event.getComponentId(), event.getFlowFileUuid());
                ProvenanceFeedStatsCalculator.instance().calculateStats(event);
            });

        }
        streamingProcessors.clear();



    }


}
