package com.thinkbiganalytics.nifi.provenance.processor;

import com.google.common.collect.Iterables;
import com.thinkbiganalytics.nifi.provenance.StreamConfiguration;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.joda.time.DateTime;

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
     * Return a KEY based upon the ProcessorId and Root flowfile event to determine if this event partakes in a STREAM or a BATCH
     */

    /*
    private String streamingMapKey(ProvenanceEventRecordDTO event) {
        ActiveFlowFile flowFile = event.getFlowFile();
        if(flowFile != null && flowFile.getFirstEvent() != null){
            return event.getComponentId();
        }
        return event.getComponentId();
    }

*/
    public abstract String streamingMapKey(ProvenanceEventRecordDTO event);

    private void movePotentialMatchingKeyToStreaming(String streamKey) {
        potentialStreamingProcessors.get(streamKey).stream().collect(Collectors.toList()).forEach(event -> addToCollection(streamingProcessors, event));

    }

    private void movePotentialToBatch() {
        //take all elements in potential collection and move them to  batch
        potentialStreamingProcessors.values().stream().flatMap(events -> events.stream()).collect(Collectors.toList()).forEach(event -> addToCollection(batchProvenanceEvents, event));
    }

    public void process(List<ProvenanceEventRecordDTO> events) {

        if (events != null && !events.isEmpty()) {
            //Process Each event as either Batch or Streaming
            events.forEach(event -> processEvent(event));
            //after processing if potential still has data then mark them as batch and clear
            movePotentialToBatch();
            //clear potential
            potentialStreamingProcessors.clear();

            processorProvenanceEvents.clear();

            //now streamingCollection and Batch collection should be populated correctly
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
                        movePotentialMatchingKeyToStreaming(key);
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
                addToCollection(batchProvenanceEvents, event);
                processingType = PROCESSING_TYPE.BATCH;
            }
        }
        //add it to processing Map
        addToCollection(processorProvenanceEvents, event);
        return processingType;
    }


    private void processBatch() {
        //handle batch event
        batchProvenanceEvents.values().forEach(event -> {

            //what do to with batch

        });

    }

    private void processStream() {
        //handle stream event

        streamingProcessors.values().forEach(event -> {

            //what to do with Stream

        });


    }


}
