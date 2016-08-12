package com.thinkbiganalytics.nifi.provenance.v2.cache.event;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sr186054 on 8/11/16. A Cache of the Provenance Events that come through Nifi This cache will be evicted from this cache once sent to jms
 */
public class ProvenanceEventCache {

    private static final long MAX_SIZE = 10000;

    private static ProvenanceEventCache instance = new ProvenanceEventCache();

    public static ProvenanceEventCache instance() {
        return instance;
    }

    private final Cache<String, ProvenanceEventRecord> eventCache;
    private final Cache<String, List<ProvenanceEventRecord>> processorEventCache;


    public static final ProvenanceEventType[] STARTING_EVENT_TYPES = {ProvenanceEventType.RECEIVE, ProvenanceEventType.CREATE};

    public static final ProvenanceEventType[] ENDING_EVENT_TYPES = {ProvenanceEventType.DROP};

    public static boolean contains(ProvenanceEventType[] allowedEvents, ProvenanceEventType event) {
        return Arrays.stream(allowedEvents).anyMatch(event::equals);
    }


    private ProvenanceEventCache() {
        eventCache = CacheBuilder.newBuilder().maximumSize(MAX_SIZE).build();
        processorEventCache = CacheBuilder.newBuilder().maximumSize(MAX_SIZE).build(new CacheLoader<String, List<ProvenanceEventRecord>>() {
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

    public List<ProvenanceEventRecord> getEventsForProcessor(String processorId) {
        return processorEventCache.getIfPresent(processorId);
    }

    public void put(ProvenanceEventRecord eventRecord) {
        //add the event to the event id map
        eventCache.put(key(eventRecord.getFlowFileUuid(), eventRecord.getEventId()), eventRecord);
        //add the event to the processor map
        List<ProvenanceEventRecord> events = processorEventCache.getIfPresent(eventRecord.getComponentId());
        events.add(eventRecord);
    }


    public static boolean isFirstEvent(ProvenanceEventRecord event) {
        return contains(STARTING_EVENT_TYPES, event.getEventType());
    }

    public static boolean isEndingEvent(ProvenanceEventRecord event) {
        return contains(ENDING_EVENT_TYPES, event.getEventType());
    }

}
