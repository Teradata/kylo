package com.thinkbiganalytics.nifi.provenance.model.util;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by sr186054 on 8/14/16.
 */
public class ProvenanceEventUtil {

    public static final String AUTO_TERMINATED_FAILURE_RELATIONSHIP = "auto-terminated by failure relationship";

    public static final String FLOWFILE_QUEUE_EMPTIED = "flowfile queue emptied";

    public static final String[] STARTING_EVENT_TYPES = {"RECEIVE","CREATE"};

    public static final String[] ENDING_EVENT_TYPES = {"DROP", "EXPIRE",};

    public static boolean contains(String[] allowedEvents, String event) {
        return Arrays.stream(allowedEvents).anyMatch(event::equals);
    }

    /**
     * Check if the event is one that kicks off the flow
     * @param event
     * @return
     */
    public static boolean isFirstEvent(ProvenanceEventRecordDTO event) {
        return contains(STARTING_EVENT_TYPES, event.getEventType());
    }


    public static boolean isEndingFlowFileEvent(ProvenanceEventRecordDTO event) {
        return contains(ENDING_EVENT_TYPES, event.getEventType());
    }

    /**
     * Certain Events in NiFi will create new Flow files and others will indicate the processor is complete
     * This will return true if the event is one that indicates the processor is complete
     * @param event
     * @return
     */
    public static boolean isCompletionEvent(ProvenanceEventRecordDTO event) {
        return true; // !contains(NON_COMPLETION_EVENTS, event.getEventType());
    }


    public static Comparator<ProvenanceEventRecordDTO> provenanceEventRecordDTOComparator() {
        return new ProvenanceEventRecordDTOComparator();
    }


    public static boolean isTerminatedByFailureRelationship(ProvenanceEventRecordDTO event) {

        return event.getDetails() != null && AUTO_TERMINATED_FAILURE_RELATIONSHIP.equalsIgnoreCase(event.getDetails());

    }

    public static boolean isFlowFileQueueEmptied(ProvenanceEventRecordDTO event) {
        return (isEndingFlowFileEvent(event) && StringUtils.isNotBlank(event.getDetails()) && event.getDetails().toLowerCase().startsWith(FLOWFILE_QUEUE_EMPTIED));
    }

}
