package com.thinkbiganalytics.nifi.provenance.cache;

/*-
 * #%L
 * thinkbig-nifi-provenance-repo
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.nifi.provenance.model.FeedFlowFile;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
        log.info("Initialize FeedFlowFileMapDbCache cache at: {}, keeping running flowfiles for {} days", fileLocation, expireAfterNumber);

        try {
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

            log.info("Successfully created FeedFlowFileMapDbCache cache at: {},  with starting size of: {} ", fileLocation, memFeedFlowFileCache.size());
        } catch (Exception e) {
            log.error("Error creating mapdb cache. {}.  If NiFi goes down with flows in progress Kylo will not be able to connect the running flows on restart to their Kylo job executions",
                      e.getMessage(), e);
            memFeedFlowFileCache = new ConcurrentHashMap<>();
            persistentFlowFileCache = new ConcurrentHashMap<>();
        }
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
            //remove any other references to this feed flowfile
            if (flowFile.getChildFlowFiles() != null) {
                flowFile.getChildFlowFiles().stream().forEach(flowFileId -> memFeedFlowFileCache.remove(flowFileId));
            }
        }
    }

    @Override
    public void onPrimaryFeedFlowsComplete(Set<String> primaryFeedFlowId) {

    }

    @Override
    public void beforeInvalidation(List<FeedFlowFile> completedFlowFiles) {
        //no op
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
            if (inMemoryDb != null) {
                inMemoryDb.commit();
                inMemoryDb.close();
            }
            if (persistentDb != null) {
                persistentDb.commit();
                persistentDb.close();
                log.info("Successfully closed the flow file MapDB cache file.");
            }
        }
        return flowFiles.size();
    }


    public void cacheFlowFile(FeedFlowFile flowFile) {
        persistentFlowFileCache.put(flowFile.getId(), flowFile);
    }


}
