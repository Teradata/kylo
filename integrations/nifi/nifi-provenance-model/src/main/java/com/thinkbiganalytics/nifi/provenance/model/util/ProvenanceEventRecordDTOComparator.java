package com.thinkbiganalytics.nifi.provenance.model.util;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import java.util.Comparator;

/**
 * Created by sr186054 on 8/17/16.
 */
public class ProvenanceEventRecordDTOComparator implements Comparator<ProvenanceEventRecordDTO> {


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
}