package com.thinkbiganalytics.nifi.provenance.v2.cache.event;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import org.apache.nifi.provenance.ProvenanceEventRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 8/11/16.
 * TODO.. is this really needed???  Currently not used
 *
 */
public class ProvenanceEventCache {

    private static final long MAX_SIZE = 1000000;

    private static ProvenanceEventCache instance = new ProvenanceEventCache();

    public static ProvenanceEventCache instance() {
        return instance;
    }

    private final Cache<String, ProvenanceEventRecord> eventCache;
    private final Cache<String, List<ProvenanceEventRecord>> processorEventCache;





    private ProvenanceEventCache() {
        eventCache = CacheBuilder.newBuilder().recordStats().maximumSize(MAX_SIZE).build();
        processorEventCache = CacheBuilder.newBuilder().recordStats().maximumSize(MAX_SIZE).build(new CacheLoader<String, List<ProvenanceEventRecord>>() {
                                                                                        @Override
                                                                                        public List<ProvenanceEventRecord> load(String id) throws Exception {
                                                                                            return new ArrayList<ProvenanceEventRecord>();
                                                                                        }
                                                                                    }
        );
    }

    public static String key(String flowFileId, Long eventId) {
        return flowFileId + "-" + eventId;
    }


    public ProvenanceEventRecord getEventById(String id) {
        return eventCache.getIfPresent(id);
    }


    public ProvenanceEventRecord getEvent(String flowFileId, Long eventId) {
        return getEventById(key(flowFileId, eventId));
    }
    public List<ProvenanceEventRecord> getEventsForProcessor(String processorId) {
        return processorEventCache.getIfPresent(processorId);
    }

    public void put(ProvenanceEventRecord eventRecord) {
        //add the event to the event id map
        eventCache.put(key(eventRecord.getFlowFileUuid(), eventRecord.getEventId()), eventRecord);
        //add the event to the processor map
 //       List<ProvenanceEventRecord> events = processorEventCache.getIfPresent(eventRecord.getComponentId());
  //      events.add(eventRecord);
    }



}
