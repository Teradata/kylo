package com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile;

import com.thinkbiganalytics.nifi.provenance.model.FlowFile;
import com.thinkbiganalytics.nifi.provenance.model.IdReferenceFlowFile;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


/**
 * Created by sr186054 on 8/19/16.
 */
public class FlowFileMapDbCache {

    private static final Logger log = LoggerFactory.getLogger(FlowFileMapDbCache.class);
    private static FlowFileMapDbCache instance = new FlowFileMapDbCache();

    public static FlowFileMapDbCache instance() {
        return instance;
    }

    private DB db;

    private final HTreeMap<String, IdReferenceFlowFile> idReferenceFlowFileHTreeMap;


    private int expireAfterNumber = 7;
    private TimeUnit expireAfterUnit = TimeUnit.DAYS;

    public FlowFileMapDbCache() {

        db = DBMaker.fileDB("flowfile-cache.db").fileMmapEnable()
            .fileMmapEnableIfSupported() // Only enable mmap on supported platforms
            .fileMmapPreclearDisable()   // Make mmap file faster
            .cleanerHackEnable()
            .checksumHeaderBypass()
            .closeOnJvmShutdown().make();

        idReferenceFlowFileHTreeMap =
            (HTreeMap<String, IdReferenceFlowFile>) db.hashMap("idRefFlowFile").keySerializer(Serializer.STRING).valueSerializer(Serializer.JAVA).expireAfterCreate(expireAfterNumber, expireAfterUnit)
                .createOrOpen();

        log.info("CREATED NEW flowFileFeedProcessGroupCache cache");


    }

    public void assignFeedInformation(FlowFile flowFile) {
        if (!flowFile.hasFeedInformationAssigned()) {
            IdReferenceFlowFile ff = idReferenceFlowFileHTreeMap.get(flowFile.getId());
            if (ff != null) {
                String feedName = ff.getFeedName();
                String processGroupId = ff.getFeedProcessGroupId();
                flowFile.assignFeedInformation(feedName, processGroupId);
            }
        }
    }

    public void cacheFlowFile(FlowFile flowFile) {
        idReferenceFlowFileHTreeMap.put(flowFile.getId(), flowFile.toIdReferenceFlowFile());
    }

    public void expire(FlowFile flowFile) {
        idReferenceFlowFileHTreeMap.remove(flowFile.getId());
    }

    public IdReferenceFlowFile getCachedFlowFile(String flowFileId) {
        return idReferenceFlowFileHTreeMap.get(flowFileId);
    }

    public void summary() {
        log.info("FlowFileCache Size: {} ", idReferenceFlowFileHTreeMap.size());

    }

}
