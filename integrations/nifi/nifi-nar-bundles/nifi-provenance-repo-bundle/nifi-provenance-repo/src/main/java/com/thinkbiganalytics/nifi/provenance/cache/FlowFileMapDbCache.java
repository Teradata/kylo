package com.thinkbiganalytics.nifi.provenance.cache;

import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.IdReferenceFlowFile;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.joda.time.DateTime;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;


/**
 * A Lightweight Map of FlowFile with ID references for Parent/children. This is persisted to disk in case NiFi crashes or goes down during flowfile execution. The cache is used by the
 * FlowFileGuavaCache to load a given flowfile. and will expire and remove itself when the FlowFileGuavaCache#expire() is called on via the TimerThread in the GuavaCache. If the cache here is not used
 * by a configurable expiration time it will invalidate itself and be removed from the cache.
 *
 * Created by sr186054 on 8/19/16.
 */
@Component
public class FlowFileMapDbCache implements FlowFileCacheListener {

    private static final Logger log = LoggerFactory.getLogger(FlowFileMapDbCache.class);
    private static FlowFileMapDbCache instance = new FlowFileMapDbCache();

    public static FlowFileMapDbCache instance() {
        return instance;
    }

    private DB db;

    private ConcurrentMap<String, IdReferenceFlowFile> idReferenceFlowFileHTreeMap;


    @Autowired
    private FlowFileGuavaCache cache;

    private int expireAfterNumber = 3;

    private TimeUnit expireAfterUnit = TimeUnit.DAYS;

    public FlowFileMapDbCache() {
    }

    @PostConstruct
    private void init() {
        log.info("Initialize FlowFileMapDbCache cache, keeping running flowfiles for {} days", expireAfterNumber);
        db = DBMaker.fileDB("flowfile-cache.db").fileMmapEnable()
            .fileMmapEnableIfSupported() // Only enable mmap on supported platforms
            .fileMmapPreclearDisable()   // Make mmap file faster
            .cleanerHackEnable()
            .checksumHeaderBypass()
                // .fileDeleteAfterOpen()
                //   .fileDeleteAfterClose()
            .closeOnJvmShutdown().make();
        //idReferenceFlowFileHTreeMap = new ConcurrentHashMap<>();
        idReferenceFlowFileHTreeMap =
            (HTreeMap<String, IdReferenceFlowFile>) db.hashMap("idRefFlowFile").keySerializer(Serializer.STRING).valueSerializer(Serializer.JAVA).expireAfterCreate(expireAfterNumber,
                                                                                                                                                                    expireAfterUnit)
                .createOrOpen();

        log.info("CREATED NEW FlowFileMapDbCache cache with starting size of: {} ", idReferenceFlowFileHTreeMap.size());

        cache.subscribe(this);
    }

    @Override
    public void onInvalidate(ActiveFlowFile flowFile) {
        if (flowFile.isBuiltFromIdReferenceFlowFile()) {
            log.debug("Removing completed flowfile {} from mapDbCache ", flowFile.getId());
            idReferenceFlowFileHTreeMap.remove(flowFile.getId());
        }
    }

    public void assignFeedInformation(ActiveFlowFile flowFile) {
        if (!flowFile.hasFeedInformationAssigned()) {
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

    public void cacheFlowFile(IdReferenceFlowFile flowFile) {
        idReferenceFlowFileHTreeMap.put(flowFile.getId(), flowFile);
    }

    public void expire(ActiveFlowFile flowFile) {
        idReferenceFlowFileHTreeMap.remove(flowFile.getId());
    }

    public IdReferenceFlowFile getCachedFlowFile(String flowFileId) {
        return idReferenceFlowFileHTreeMap.get(flowFileId);
    }

    public void summary() {
        log.info("FlowFileMapDbCache Size: {} ", idReferenceFlowFileHTreeMap.size());

    }


    public int loadGuavaCache() {
        Collection<IdReferenceFlowFile> flowFiles = idReferenceFlowFileHTreeMap.values();
        //just need to load the root files.  the children will be loaded in the graph
        Set<IdReferenceFlowFile> rootFlowFiles = flowFiles.stream().filter(idReferenceFlowFile1 -> idReferenceFlowFile1.isRootFlowFile()).collect(Collectors.toSet());

        log.info("About to Load {} flow files to Guava Cache, {} which are root flow files ", flowFiles.size(), rootFlowFiles.size());
        rootFlowFiles.stream().forEach(idReferenceFlowFile -> {
            loadGraph(idReferenceFlowFile);
        });
        log.info("Finished loading {} flow files to Guava Cache. ", flowFiles.size());
        cache.printSummary();
        return flowFiles.size();
    }


    public int persistActiveRootFlowFiles() {
        Collection<ActiveFlowFile> flowFiles = cache.getAllFlowFiles();
        log.info("About to persist {}  flow files to disk via MapDB ", flowFiles.size());
        flowFiles.stream().map(flowFile -> flowFile.toIdReferenceFlowFile()).forEach(idReferenceFlowFile -> cacheFlowFile(idReferenceFlowFile));
        log.info("Successfully persisted {}  flow files to disk via MapDB.  Persisted Map Size is: {} entries ", flowFiles.size(), idReferenceFlowFileHTreeMap.size());
        return flowFiles.size();
    }


    /**
     * Load the FlowFile from the saved "IdReference MapDB " or creates a new FlowFile object
     */
    private ActiveFlowFile loadGraph(IdReferenceFlowFile parentFlowFile) {
        log.debug("Loading IdReferenceFlowFile into guava cache with id: {}  isRoot: {} ", parentFlowFile.getId(), parentFlowFile.isRootFlowFile());

        ActiveFlowFile parent = cache.getEntry(parentFlowFile.getId());
        parent.setIsBuiltFromIdReferenceFlowFile(true);
        parent.assignFeedInformation(parentFlowFile.getFeedName(), parentFlowFile.getFeedProcessGroupId());

        if (parentFlowFile.getPreviousEventId() != null) {
            parent.setPreviousEventForEvent(constructPreviousEvent(parentFlowFile));
        }

        if (parentFlowFile.isRootFlowFile()) {
            parent.markAsRootFlowFile();
            ProvenanceEventRecordDTO firstEvent = constructFirstEvent(parentFlowFile);
            parent.setFirstEvent(firstEvent);
            firstEvent.setFlowFile(parent);
        }
        parent.setCurrentFlowFileComplete(parentFlowFile.isComplete());

        for (String childId : parentFlowFile.getChildIds()) {
            IdReferenceFlowFile idReferenceFlowFile = getCachedFlowFile(childId);
            if (idReferenceFlowFile != null) {
                ActiveFlowFile child = loadGraph(idReferenceFlowFile);
                parent.addChild(child);
                child.addParent(parent);
            } else {
                log.warn(
                    "Warning.  Unable to load {} persisted IdReferenceFlowFile into the Cache.  It is not in the persisted cache.  Events related to this flowfile may not get completed properly in Kylo. ",
                    childId);
            }
        }

        return parent;
    }

    /**
     * Construct what is needed from the idRef file to create the First event of the root flow file
     */
    private ProvenanceEventRecordDTO constructFirstEvent(IdReferenceFlowFile rootFlowFile) {
        ProvenanceEventRecordDTO firstEvent = new ProvenanceEventRecordDTO();
        firstEvent.setEventId(rootFlowFile.getRootFlowFileFirstEventId());
        firstEvent.setEventTime(new DateTime(rootFlowFile.getRootFlowFileFirstEventTime()));
        firstEvent.setEventType(rootFlowFile.getRootFlowFileFirstEventType());
        firstEvent.setComponentId(rootFlowFile.getRootFlowFileFirstEventComponentId());
        firstEvent.setComponentName(rootFlowFile.getRootFlowFileFirstEventComponentName());
        firstEvent.setFeedName(rootFlowFile.getFeedName());
        firstEvent.setFeedProcessGroupId(rootFlowFile.getFeedProcessGroupId());
        firstEvent.setStartTime(new DateTime(rootFlowFile.getRootFlowFileFirstEventStartTime()));
        return firstEvent;
    }


    /**
     * Construct what is needed from the idRef file to create the First event of the root flow file
     */
    private ProvenanceEventRecordDTO constructPreviousEvent(IdReferenceFlowFile flowFile) {
        ProvenanceEventRecordDTO event = new ProvenanceEventRecordDTO();
        event.setEventId(flowFile.getPreviousEventId());
        event.setEventTime(new DateTime(flowFile.getPreviousEventTime()));
        event.setFeedName(flowFile.getFeedName());
        event.setFeedProcessGroupId(flowFile.getFeedProcessGroupId());
        return event;
    }

}
