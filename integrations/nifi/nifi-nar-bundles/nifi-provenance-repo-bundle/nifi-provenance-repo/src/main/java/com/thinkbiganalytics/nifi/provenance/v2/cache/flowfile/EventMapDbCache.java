package com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile;

import com.thinkbiganalytics.nifi.provenance.model.FlowFile;
import com.thinkbiganalytics.nifi.provenance.model.FlowFileEvent;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.v2.cache.CacheUtil;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by sr186054 on 8/20/16.
 */
public class EventMapDbCache {

    private static final Logger log = LoggerFactory.getLogger(EventMapDbCache.class);
    private static EventMapDbCache instance = new EventMapDbCache();

    public static EventMapDbCache instance() {
        return instance;
    }

    private DB db;

    private final ConcurrentMap<Long, ProvenanceEventRecordDTO> cache;
    private boolean active = false;


    public EventMapDbCache() {
        if (active) {
            db = DBMaker.fileDB("event-cache.db").fileMmapEnable()
                .fileMmapEnableIfSupported() // Only enable mmap on supported platforms
                .fileMmapPreclearDisable()   // Make mmap file faster
                    //       .fileDeleteAfterOpen()
                    //   .fileDeleteAfterClose()

                    // Unmap (release resources) file when its closed.
                    // That can cause JVM crash if file is accessed after it was unmapped
                    // (there is possible race condition).
                .cleanerHackEnable()
                .checksumHeaderBypass()
                .closeOnJvmShutdown().make();
            cache = (HTreeMap<Long, ProvenanceEventRecordDTO>) db.hashMap("eventMap").keySerializer(Serializer.LONG).valueSerializer(Serializer.JAVA).createOrOpen();
            //  init();
            load();
            log.info("CREATED NEW MAPDB cache");
        } else {
            log.info("EventMapCache is not active ");
            cache = new ConcurrentHashMap<>();
        }
    }


    public ProvenanceEventRecordDTO getEntry(String id) {
        return cache.get(id);
    }

    private void load() {
        long start = System.currentTimeMillis();
        TreeSet<Long> sortedIds = new TreeSet<>(cache.keySet());
        for (Long eventId : sortedIds) {
            CacheUtil.instance().cache(cache.get(eventId));
        }
        long stop = System.currentTimeMillis();
        log.info("Time to load {} events {} ms", sortedIds.size(), (stop - start));

    }

    public void expire(FlowFile flowFile) {
        if (active) {
            List<FlowFileEvent> events = (List<FlowFileEvent>) flowFile.getCompletedEvents();
            for (FlowFileEvent event : events) {
                cache.remove(event.getEventId());
            }
        }
    }

    public void save(ProvenanceEventRecordDTO event) {
        if (active) {
            cache.put(event.getEventId(), event);
        }
        //  db.commit();
    }


}
