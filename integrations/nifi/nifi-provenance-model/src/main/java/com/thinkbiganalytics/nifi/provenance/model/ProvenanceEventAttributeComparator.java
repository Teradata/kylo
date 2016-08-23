package com.thinkbiganalytics.nifi.provenance.model;

import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by sr186054 on 8/19/16.
 */
public class ProvenanceEventAttributeComparator implements Comparator<ProvenanceEventAttributeDTO>, Serializable {

    @Override
    public int compare(ProvenanceEventAttributeDTO a1, ProvenanceEventAttributeDTO a2) {
        return Collator.getInstance(Locale.US).compare(a1.getName(), a2.getName());
    }
}

