/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

import org.apache.nifi.provenance.PersistentProvenanceRepository;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by matthutton on 2/21/16.
 */
public class  ThinkBigProvenanceEventRepository extends PersistentProvenanceRepository implements ProvenanceEventRepository {

    public ThinkBigProvenanceEventRepository() throws IOException {
        super();
    }


    public void registerEvent(ProvenanceEventRecord event) {
        try {
            System.out.println("Received event!!");
            System.out.println(event.getComponentType());
            System.out.println(event.getComponentId());
            System.out.println(event.getAlternateIdentifierUri());
            System.out.println(event.getFlowFileUuid());
            System.out.println(event.getSourceSystemFlowFileIdentifier());
            List<String> parentUuids = event.getParentUuids();
            for (String pid : parentUuids) {
                System.out.println("pid: " + pid);
            }
            System.out.println("Details: " + event.getDetails());
            System.out.println("event type: " + event.getEventType().toString());

            Map<String, String> atts = event.getAttributes();
            for (String s : atts.keySet()) {
                System.out.println("  " + s + ":" + atts.get(s));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.registerEvent(event);
    }


}
