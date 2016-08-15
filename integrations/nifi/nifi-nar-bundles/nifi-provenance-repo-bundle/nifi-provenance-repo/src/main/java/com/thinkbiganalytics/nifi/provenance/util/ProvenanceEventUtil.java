package com.thinkbiganalytics.nifi.provenance.util;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by sr186054 on 8/14/16.
 */
public class ProvenanceEventUtil {

    public static final ProvenanceEventType[] STARTING_EVENT_TYPES = {ProvenanceEventType.RECEIVE, ProvenanceEventType.CREATE};

    public static final ProvenanceEventType[] ENDING_EVENT_TYPES = {ProvenanceEventType.DROP};


    public static final ProvenanceEventType[] NON_COMPLETION_EVENTS = {ProvenanceEventType.SEND, ProvenanceEventType.CLONE, ProvenanceEventType.ROUTE};

    public static boolean contains(ProvenanceEventType[] allowedEvents, ProvenanceEventType event) {
        return Arrays.stream(allowedEvents).anyMatch(event::equals);
    }

    public static boolean isFirstEvent(ProvenanceEventDTO event) {
        return contains(STARTING_EVENT_TYPES, ProvenanceEventType.valueOf(event.getEventType()));
    }

    public static boolean isFirstEvent(ProvenanceEventRecord event) {
        return contains(STARTING_EVENT_TYPES, event.getEventType());
    }

    public static boolean isEndingEvent(ProvenanceEventRecord event) {
        return contains(ENDING_EVENT_TYPES, event.getEventType());
    }

    public static boolean isCompletionEvent(ProvenanceEventRecord event) {
        return !contains(NON_COMPLETION_EVENTS, event.getEventType());
    }

    public static boolean isCompletionEvent(ProvenanceEventDTO event) {
        return !contains(NON_COMPLETION_EVENTS, ProvenanceEventType.valueOf(event.getEventType()));
    }


    public static Comparator<ProvenanceEventRecordDTO> provenanceEventRecordDTOComparator() {
        return new Comparator<ProvenanceEventRecordDTO>() {
            @Override
            public int compare(ProvenanceEventRecordDTO o1, ProvenanceEventRecordDTO o2) {
                if (o1 == null && o1 == null) {
                    return 0;
                } else if (o1 != null && o2 == null) {
                    return -1;
                } else if (o1 == null && o2 != null) {
                    return 1;
                } else {
                    int compare = o1.getEventTime().compareTo(o2.getEventTime());
                    if (compare == 0) {
                        compare = o1.getEventId().compareTo(o2.getEventId());
                    }
                    return compare;
                }
            }
        };
    }

}
