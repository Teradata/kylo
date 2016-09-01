package com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile;

import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.IdReferenceFlowFile;

import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


/**
 * A Lightweight Map of FlowFile with ID references for Parent/children.
 * This is persisted to disk in case NiFi crashes or goes down during flowfile execution.
 * The cache is used by the FlowFileGuavaCache to load a given flowfile. and will expire and remove itself when the FlowFileGuavaCache#expire() is called on via the TimerThread in the GuavaCache.
 * If the cache here is not used by a configurable expiration time it will invalidate itself and be removed from the cache.
 *
 * Created by sr186054 on 8/19/16.
 */
public class FlowFileMapDbCache {

    private static final Logger log = LoggerFactory.getLogger(FlowFileMapDbCache.class);
    private static FlowFileMapDbCache instance = new FlowFileMapDbCache();

    public static FlowFileMapDbCache instance() {
        return instance;
    }

    private DB db;

    private final ConcurrentMap<String, IdReferenceFlowFile> idReferenceFlowFileHTreeMap;


    private int expireAfterNumber = 7;
    private TimeUnit expireAfterUnit = TimeUnit.DAYS;

    public FlowFileMapDbCache() {

        //    db = DBMaker.fileDB("flowfile-cache.db").fileMmapEnable()
        //        .fileMmapEnableIfSupported() // Only enable mmap on supported platforms
        //       .fileMmapPreclearDisable()   // Make mmap file faster
        //       .cleanerHackEnable()
        //      .checksumHeaderBypass()
        //     .closeOnJvmShutdown().make();
        idReferenceFlowFileHTreeMap = new ConcurrentHashMap<>();
        //    idReferenceFlowFileHTreeMap =
        //        (HTreeMap<String, IdReferenceFlowFile>) db.hashMap("idRefFlowFile").keySerializer(Serializer.STRING).valueSerializer(Serializer.JAVA).expireAfterCreate(expireAfterNumber, expireAfterUnit)
        //           .createOrOpen();


        log.info("CREATED NEW flowFileFeedProcessGroupCache cache");


    }

    public void assignFeedInformation(ActiveFlowFile flowFile) {
        if (false && !flowFile.hasFeedInformationAssigned()) {
            IdReferenceFlowFile ff = idReferenceFlowFileHTreeMap.get(flowFile.getId());
            if (ff != null) {
                String feedName = ff.getFeedName();
                String processGroupId = ff.getFeedProcessGroupId();
                flowFile.assignFeedInformation(feedName, processGroupId);
            }
        }
    }

    public void cacheFlowFile(ActiveFlowFile flowFile) {
        idReferenceFlowFileHTreeMap.put(flowFile.getId(), flowFile.toIdReferenceFlowFile());
    }

    public void expire(ActiveFlowFile flowFile) {
        idReferenceFlowFileHTreeMap.remove(flowFile.getId());
    }

    public IdReferenceFlowFile getCachedFlowFile(String flowFileId) {
        return idReferenceFlowFileHTreeMap.get(flowFileId);
    }

    public void summary() {
        log.info("FlowFileCache Size: {} ", idReferenceFlowFileHTreeMap.size());

    }

}
