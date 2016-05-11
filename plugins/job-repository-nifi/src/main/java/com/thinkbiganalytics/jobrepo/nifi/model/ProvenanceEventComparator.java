package com.thinkbiganalytics.jobrepo.nifi.model;

import java.util.Comparator;

/**
 * Created by sr186054 on 2/29/16.
 */
public class ProvenanceEventComparator implements Comparator<ProvenanceEventRecordDTO> {

    @Override
    public int compare(ProvenanceEventRecordDTO o1, ProvenanceEventRecordDTO o2) {
        int compare = o1.getEventTime().compareTo(o2.getEventTime());
        if (compare == 0) {
            compare = new Long(o1.getEventId()).compareTo(new Long(o2.getEventId()));
        }
        return compare;
    }
}