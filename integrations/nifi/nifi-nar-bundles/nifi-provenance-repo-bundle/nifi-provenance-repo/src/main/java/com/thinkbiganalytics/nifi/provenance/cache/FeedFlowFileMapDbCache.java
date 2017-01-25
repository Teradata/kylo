package com.thinkbiganalytics.nifi.provenance.cache;

import com.thinkbiganalytics.nifi.provenance.model.FeedFlowFile;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

/**
 * Persist any running flowfiles to disk when NiFi shuts down to maintain the processing feed status when NiFi comes back up
 */
public class FeedFlowFileMapDbCache implements FeedFlowFileCacheListener {

    private static final Logger log = LoggerFactory.getLogger(FeedFlowFileMapDbCache.class);

    /**
     * the in memory MapDb database
     */
    private DB inMemoryDb;

    /**
     * The cache loaded from Disk an put into Memory.
     */
    private ConcurrentMap<String, FeedFlowFile> memFeedFlowFileCache;


    /**
     * the persistent mapdb database
     */
    private DB persistentDb;

    /**
     * The file that will be persisted to disk
     */
    private ConcurrentMap<String, FeedFlowFile> persistentFlowFileCache;


    @Autowired
    private FeedFlowFileGuavaCache cache;

    private int expireAfterNumber = 3;

    private TimeUnit expireAfterUnit = TimeUnit.DAYS;


    public FeedFlowFileMapDbCache(String fileLocation) {
        log.info("Initialize FeedFlowFileMapDbCache cache, keeping running flowfiles for {} days", expireAfterNumber);

        //delete the file after its loaded/opened
        inMemoryDb = DBMaker.fileDB(fileLocation).fileMmapEnable()
            .fileMmapEnableIfSupported() // Only enable mmap on supported platforms
            .fileMmapPreclearDisable()   // Make mmap file faster
            .cleanerHackEnable()
            .checksumHeaderBypass()
            .fileDeleteAfterOpen()
            .closeOnJvmShutdown().make();
        memFeedFlowFileCache =
            (HTreeMap<String, FeedFlowFile>) inMemoryDb.hashMap("feedFlowFile").keySerializer(Serializer.STRING).valueSerializer(Serializer.JAVA).expireAfterCreate(expireAfterNumber,
                                                                                                                                                                    expireAfterUnit)
                .createOrOpen();

        //create a new db that will be used when persisting the data to disk
        persistentDb = DBMaker.fileDB(fileLocation).fileMmapEnable()
            .fileMmapEnableIfSupported() // Only enable mmap on supported platforms
            .fileMmapPreclearDisable()   // Make mmap file faster
            .cleanerHackEnable()
            .checksumHeaderBypass()
            .closeOnJvmShutdown().make();
        persistentFlowFileCache =
            (HTreeMap<String, FeedFlowFile>) persistentDb.hashMap("feedFlowFile").keySerializer(Serializer.STRING).valueSerializer(Serializer.JAVA)
                .createOrOpen();

        log.info("CREATED NEW FeedFlowFileMapDbCache cache with starting size of: {} ", memFeedFlowFileCache.size());
    }

    public FeedFlowFileMapDbCache() {
        this("feed-flowfile-cache.db");

    }


    @PostConstruct
    private void init() {
        cache.subscribe(this);
    }


    /**
     * When the {@link FeedFlowFileGuavaCache} is invalidated then it is also removed from the persistent disk storage if it exists.
     */
    public void onInvalidate(FeedFlowFile flowFile) {
        if (flowFile.isBuiltFromMapDb()) {
            log.debug("Removing completed flowfile {} from mapDbCache ", flowFile.getId());
            memFeedFlowFileCache.remove(flowFile.getId());
        }
    }

    /**
     * Load the persisted cached back into the {@link FeedFlowFileGuavaCache}
     */
    public int loadGuavaCache() {

        memFeedFlowFileCache.values().stream().forEach(feedFlowFile -> {
            feedFlowFile.setBuiltFromMapDb(true);
            cache.add(feedFlowFile.getId(), feedFlowFile);
            if (feedFlowFile.getActiveChildFlowFiles() != null) {
                feedFlowFile.getActiveChildFlowFiles().stream().forEach(feedFlowFileId -> cache.add(feedFlowFileId, feedFlowFile));
            }
        });
        return memFeedFlowFileCache.values().size();


    }

    /**
     * return the size of the MapDB Cache
     */
    public Integer size() {
        return memFeedFlowFileCache.size();
    }


    public Collection<FeedFlowFile> getCache() {
        return memFeedFlowFileCache.values();
    }

    /**
     * Persist the {@link FeedFlowFileGuavaCache} to disk
     */
    public int persistFlowFiles() {
        Collection<FeedFlowFile> flowFiles = cache.getFlowFiles();
        log.info("About to persist {}  flow files to disk via MapDB ", flowFiles.size());
        flowFiles.stream().forEach(feedFlowFile -> cacheFlowFile(feedFlowFile));
        log.info("Successfully persisted {}  flow files to disk via MapDB.  Persisted Map Size is: {} entries ", flowFiles.size(), persistentFlowFileCache.size());
        if (flowFiles != null && !flowFiles.isEmpty()) {
            inMemoryDb.commit();
            inMemoryDb.close();
            persistentDb.commit();
            persistentDb.close();
            log.info("Successfully closed the flow file MapDB cache file.");
        }
        return flowFiles.size();
    }


    public void cacheFlowFile(FeedFlowFile flowFile) {
        persistentFlowFileCache.put(flowFile.getId(), flowFile);
    }


}
