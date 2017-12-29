package com.thinkbiganalytics.nifi.provenance.repo;


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

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.nifi.provenance.util.ProvenanceEventUtil;

import org.apache.commons.io.serialization.ValidatingObjectInputStream;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Hold all data about running flows as they pertain to Feeds
 */
public class FeedEventStatistics implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(FeedEventStatistics.class);

    protected Map<String, String> feedFlowFileIdToFeedProcessorId = new ConcurrentHashMap<>();

    ////Track feedFlowFile relationships to parent/child flow files for lifetime feed job execution
    protected Set<String> detailedTrackingFeedFlowFileId = new HashSet<>();

    /**
     * Map of all the flow files as they pertain to the starting feed flow file
     * Used to expire EventStatistics
     */
    protected Map<String, String> allFlowFileToFeedFlowFile = new ConcurrentHashMap<>();


    /**
     * Removal Listener for those events that are tracked and sent to Kylo Operations Manager.
     * Events with Detailed Tracking need to be added to the special cache to ensure step capture and timing information.
     * DROP events on Flowfiles indicate the end of the flow file.  Flows that split/merge will relate to other flow files.  Sometimes the DROP of the previous flow file will
     * be processed in a different order than the next flow file.  Beacuse of this the removal of data needs to be done after the fact to ensure the flows capture the correct data in ops manager
     */
    RemovalListener<Long, String> flowFileRemovalListener = new RemovalListener<Long, String>() {
        @Override
        public void onRemoval(RemovalNotification<Long, String> removalNotification) {
            Long eventId = removalNotification.getKey();
            String flowFileId = removalNotification.getValue();
            clearData(eventId, flowFileId);
        }
    };

    /**
     * An Expiring cache of the flowfile information that is tracked and Sent to Kylo Ops Manager as ProvenanceEventDTO objects
     * Map<EventId, eventFlowFileId>.  The eventId is tied to the flowfile id that initiated the final DROP event type
     * Wait 1 minute before expiring and cleaning up these resources.
     **/
    protected Cache<Long, String> detailedTrackingFlowFilesToDelete = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .removalListener(flowFileRemovalListener)
        .build();

    ///Track Timing Information for each event


    /**
     * Map of the FlowFile Id to Event Time that is not a drop event
     */
    protected Map<String, Long> flowFileLastNonDropEventTime = new ConcurrentHashMap<>();

    /**
     * Map of the EventId to the duration in millis
     */
    protected Map<Long, Long> eventDuration = new ConcurrentHashMap<>();

    protected Map<Long, Long> eventStartTime = new ConcurrentHashMap<>();

    /**
     * feed flowFile Id to startTime
     */
    protected Map<String, Long> feedFlowFileStartTime = new ConcurrentHashMap<>();

    /**
     * feedFlowFile Id to end time
     */
    protected Map<String, Long> feedFlowFileEndTime = new ConcurrentHashMap<>();

    //Feed Execution tracking

    private Map<String, long[]> flowRate = new HashMap<>();

    /**
     * Set of Event Ids that are events that finish the feed flow execution.  Last Job Event Ids
     */
    protected Set<Long> eventsThatCompleteFeedFlow = new HashSet<>();

    /**
     * Count of how many flow files are still processing for a given feedFlowFile execution
     */
    protected Map<String, AtomicInteger> feedFlowProcessing = new ConcurrentHashMap<>();

    /// Skipped detail tracking and failure counts

    /**
     * Events not capturing details
     */
    protected AtomicLong skippedEvents = new AtomicLong(0);


    /**
     * Count of how many failures have been detected for a feedFlowFile execution
     */
    protected Map<String, AtomicInteger> feedFlowFileFailureCount = new ConcurrentHashMap<>();


    /**
     * Count of the flows running by feed processor
     */
    protected Map<String,AtomicLong> feedProcessorRunningFeedFlows = new ConcurrentHashMap<>();

    /**
     * Count of the flows running by feed processor
     */
    protected Set<String> changedFeedProcessorRunningFeedFlows = new HashSet<>();

    protected AtomicBoolean feedProcessorRunningFeedFlowsChanged = new AtomicBoolean(false);

    /**
     * file location to persist this data if NiFi goes Down midstream
     * This value is set via the KyloPersistenetProvenanceEventRepository during initialization
     */
    private String backupLocation = "/opt/nifi/feed-event-statistics.gz";

    /**
     * Map of the NiFi Event to Nifi Class that should be skipped
     */
    private Map<String, Set<String>> eventTypeProcessorTypeSkipChildren = new HashMap<>();


    private static final FeedEventStatistics instance = new FeedEventStatistics();

    private FeedEventStatistics() {

    }

    public static FeedEventStatistics getInstance() {
        return instance;
    }

    public String getBackupLocation() {
        return backupLocation;
    }

    public void setBackupLocation(String backupLocation) {
        this.backupLocation = backupLocation;
    }

    private boolean shouldSkipChildren(ProvenanceEventType eventType, String componentType) {
        boolean
            skip =
            componentType != null && eventType != null && eventTypeProcessorTypeSkipChildren != null && eventTypeProcessorTypeSkipChildren.containsKey(eventType.name())
            && eventTypeProcessorTypeSkipChildren
                   .get(eventType.name()) != null && eventTypeProcessorTypeSkipChildren
                .get(eventType.name()).stream().anyMatch(type -> componentType.equalsIgnoreCase(type));
        if (skip) {
            //log it
            log.info("Skip processing children flow files {} for {} ", eventType, componentType);
        }
        return skip;
    }

    public void updateEventTypeProcessorTypeSkipChildren(String json) {
        try {
            Map<String, Set<String>> m = ObjectMapperSerializer.deserialize(json, new TypeReference<Map<String, Set<String>>>() {
            });
            this.eventTypeProcessorTypeSkipChildren = m;
            log.info("Reset the Orphan Flowfile processor Map with {} ", json);
        } catch (Exception e) {
            log.error("Unable to update the {}.  Invalid JSON supplied {} ", ConfigurationProperties.ORPHAN_CHILD_FLOW_FILE_PROCESSORS_KEY, e.getMessage(), e);
        }

    }

    public boolean backup() {
        return backup(getBackupLocation());
    }

    public boolean backup(String location) {

        try {
            //cleanup any files that should be removed before backup
            detailedTrackingFlowFilesToDelete.cleanUp();

            FileOutputStream fos = new FileOutputStream(location);
            GZIPOutputStream gz = new GZIPOutputStream(fos);

            ObjectOutputStream oos = new ObjectOutputStream(gz);

            oos.writeObject(new FeedEventStatisticsData(this));
            oos.close();
            return true;

        } catch (Exception ex) {
            log.error("Error backing up feed event statistics to {}. {} ", location, ex.getMessage(), ex);
        }
        return false;
    }

    public boolean loadBackup() {
        return loadBackup(getBackupLocation());
    }

    public boolean loadBackup(String location) {
        FeedEventStatisticsData inStats = null;
        try {

            FileInputStream fin = new FileInputStream(location);
            GZIPInputStream gis = new GZIPInputStream(fin);
            ValidatingObjectInputStream ois = new ValidatingObjectInputStream(gis);
            ois.accept(FeedEventStatisticsData.class);
            inStats = (FeedEventStatisticsData) ois.readObject();
            ois.close();

        } catch (Exception ex) {
            if (!(ex instanceof FileNotFoundException)) {
                log.error("Unable to load feed event statistics backup from {}. {} ", location, ex.getMessage(), ex);
            } else {
                log.info("Kylo feed event statistics backup file not found. Not loading backup from {}. ", location);
            }
        }
        if (inStats != null) {
            boolean success = this.load(inStats);
            //DELETE backup
            try {
                File f = new File(location);
                if (f.exists()) {
                    if(! f.delete()) {
                        throw new RuntimeException("Error deleting file " + f.getName());
                    }
                }
            } catch (Exception e) {

            }
            return success;
        }
        return false;
    }


    public void clear() {
        this.feedFlowFileIdToFeedProcessorId.clear();
        this.detailedTrackingFeedFlowFileId.clear();
        this.allFlowFileToFeedFlowFile.clear();
        this.flowFileLastNonDropEventTime.clear();
        this.eventDuration.clear();
        this.eventStartTime.clear();
        this.feedFlowFileStartTime.clear();
        this.feedFlowFileEndTime.clear();
        this.eventsThatCompleteFeedFlow.clear();
        this.feedFlowProcessing.clear();
        this.skippedEvents.set(0L);
        this.feedFlowFileFailureCount.clear();
    }


    public boolean load(FeedEventStatisticsData other) {

        // clear();
        this.feedFlowFileIdToFeedProcessorId.putAll(other.feedFlowFileIdToFeedProcessorId);
        this.detailedTrackingFeedFlowFileId.addAll(other.detailedTrackingFeedFlowFileId);
        this.allFlowFileToFeedFlowFile.putAll(other.allFlowFileToFeedFlowFile);
        this.flowFileLastNonDropEventTime.putAll(other.flowFileLastNonDropEventTime);
        this.eventDuration.putAll(other.eventDuration);
        this.eventStartTime.putAll(other.eventStartTime);
        this.feedFlowFileStartTime.putAll(other.feedFlowFileStartTime);
        this.feedFlowFileEndTime.putAll(other.feedFlowFileEndTime);
        this.eventsThatCompleteFeedFlow.addAll(other.eventsThatCompleteFeedFlow);
        this.feedFlowProcessing.putAll(other.feedFlowProcessing);
        this.skippedEvents.set(other.skippedEvents.get());
        this.feedFlowFileFailureCount.putAll(other.feedFlowFileFailureCount);
        return true;


    }


    public void checkAndAssignStartingFlowFile(ProvenanceEventRecord event) {
        if (ProvenanceEventUtil.isStartingFeedFlow(event)) {
            //startingFlowFiles.add(event.getFlowFileUuid());
            allFlowFileToFeedFlowFile.put(event.getFlowFileUuid(), event.getFlowFileUuid());
            //add the flow to active processing
            feedFlowProcessing.computeIfAbsent(event.getFlowFileUuid(), feedFlowFileId -> new AtomicInteger(0)).incrementAndGet();
            feedFlowFileIdToFeedProcessorId.put(event.getFlowFileUuid(), event.getComponentId());

            feedProcessorRunningFeedFlows.computeIfAbsent(event.getComponentId(),processorId -> new AtomicLong(0)).incrementAndGet();
            feedProcessorRunningFeedFlowsChanged.set(true);
            changedFeedProcessorRunningFeedFlows.add(event.getComponentId());
            //  feedFlowToRelatedFlowFiles.computeIfAbsent(event.getFlowFileUuid(), feedFlowFileId -> new HashSet<>()).add(event.getFlowFileUuid());
        }
    }

    public void markFeedProcessorRunningFeedFlowsUnchanged(){
        feedProcessorRunningFeedFlowsChanged.set(false);
        changedFeedProcessorRunningFeedFlows.clear();
    }

    public boolean isFeedProcessorRunningFeedFlowsChanged(){
        return feedProcessorRunningFeedFlowsChanged.get();
    }

    /**
     * attach the event that has parents/children to a tracking feedflowfile (if possible)
     * This is for the Many to one case
     *
     * @param event the event
     * @return the parent event to track
     */
    private String determineParentFeedFlow(ProvenanceEventRecord event) {
        String feedFlowFile = null;
        String parent = event.getParentUuids().stream().filter(parentFlowFileId -> isTrackingDetails(parentFlowFileId)).findFirst().orElse(null);
        if (parent == null) {
            parent = event.getParentUuids().get(0);
        }
        if (parent != null) {
            feedFlowFile = getFeedFlowFileId(parent);
        }

        return feedFlowFile;
    }


    public boolean assignParentsAndChildren(ProvenanceEventRecord event) {

        //Assign the Event to one of the Parents

        //  activeFlowFiles.add(event.getFlowFileUuid());
        String startingFlowFile = allFlowFileToFeedFlowFile.get(event.getFlowFileUuid());
        boolean trackingEventFlowFile = false;
        if (event.getParentUuids() != null && !event.getParentUuids().isEmpty()) {

            if (startingFlowFile == null) {
                startingFlowFile = determineParentFeedFlow(event);
                if (startingFlowFile != null) {
                    allFlowFileToFeedFlowFile.put(event.getFlowFileUuid(), startingFlowFile);
                    if (feedFlowProcessing.containsKey(startingFlowFile)) {
                        feedFlowProcessing.get(startingFlowFile).incrementAndGet();
                        trackingEventFlowFile = true;
                    }
                }
            }


        }
        if (startingFlowFile != null && event.getChildUuids() != null && !event.getChildUuids().isEmpty() && !shouldSkipChildren(event.getEventType(), event.getComponentType())) {
            for (String child : event.getChildUuids()) {
                allFlowFileToFeedFlowFile.put(child, startingFlowFile);
                //Add children flow files to active processing
                //skip this add if we already did it while iterating the parents.
                //NiFi will create a new Flow File for this event (event.getFlowFileId) and it will also be part of the children
                if (feedFlowProcessing.containsKey(startingFlowFile) && (!trackingEventFlowFile || (trackingEventFlowFile && !child.equalsIgnoreCase(event.getFlowFileUuid())))) {
                    feedFlowProcessing.get(startingFlowFile).incrementAndGet();
                }
                flowFileLastNonDropEventTime.put(child, event.getEventTime());
            }
        }

        return startingFlowFile != null;

    }


    public void calculateTimes(ProvenanceEventRecord event, Long eventId) {
        //  eventIdEventTime.put(eventId,event.getEventTime());
        Long startTime = lastEventTimeForFlowFile(event.getFlowFileUuid());
        if (startTime == null && hasParents(event)) {
            startTime = lastEventTimeForParent(event.getParentUuids());
        }
        if (startTime == null) {
            startTime = event.getFlowFileEntryDate();
        }
        DateTime st = new DateTime(startTime);
        if (ProvenanceEventUtil.isStartingFeedFlow(event)) {
            feedFlowFileStartTime.put(event.getFlowFileUuid(), startTime);
        }

        Long duration = event.getEventTime() - startTime;
        eventDuration.put(eventId, duration);
        eventStartTime.put(eventId, startTime);

        if (!ProvenanceEventType.DROP.equals(event.getEventType())) {
            flowFileLastNonDropEventTime.put(event.getFlowFileUuid(), event.getEventTime());
        }

    }

    public void skip(ProvenanceEventRecord event, Long eventId) {
        skippedEvents.incrementAndGet();
    }


    private Long lastEventTimeForFlowFile(String flowFile) {
        return flowFileLastNonDropEventTime.get(flowFile);
    }


    private Long lastEventTimeForParent(Collection<String> parentIds) {
        return parentIds.stream().filter(flowFileId -> flowFileLastNonDropEventTime.containsKey(flowFileId)).findFirst().map(flowFileId -> flowFileLastNonDropEventTime.get(flowFileId)).orElse(null);
    }


    public boolean isEndingFeedFlow(Long eventId) {
        return eventsThatCompleteFeedFlow.contains(eventId);
    }


    /**
     * are we tracking details for this feed
     */
    public boolean isTrackingDetails(String eventFlowFileId) {
        String feedFlowFile = allFlowFileToFeedFlowFile.get(eventFlowFileId);
        if (feedFlowFile != null) {
            return detailedTrackingFeedFlowFileId.contains(feedFlowFile);
        }
        return false;
    }

    public void setTrackingDetails(ProvenanceEventRecord event) {
        detailedTrackingFeedFlowFileId.add(event.getFlowFileUuid());
    }

    private boolean hasParents(ProvenanceEventRecord event) {
        return event.getParentUuids() != null && !event.getParentUuids().isEmpty();
    }


    public Long getEventDuration(Long eventId) {
        return eventDuration.get(eventId);
    }

    public Long getEventStartTime(Long eventId) {
        return eventStartTime.get(eventId);
    }


    public boolean hasFailures(ProvenanceEventRecord event) {
        String feedFlowFile = getFeedFlowFileId(event);
        if (feedFlowFile != null) {
            return feedFlowFileFailureCount.getOrDefault(feedFlowFile, new AtomicInteger(0)).get() > 0;
        }
        return false;
    }

    public Long getSkippedEvents() {
        return skippedEvents.get();
    }

    public String getFeedFlowFileId(ProvenanceEventRecord event) {
        return allFlowFileToFeedFlowFile.get(event.getFlowFileUuid());
    }

    public String getFeedFlowFileId(String eventFlowFileId) {
        return allFlowFileToFeedFlowFile.get(eventFlowFileId);
    }


    public String getFeedProcessorId(ProvenanceEventRecord event) {
        String feedFlowFileId = getFeedFlowFileId(event);
        return feedFlowFileId != null ? feedFlowFileIdToFeedProcessorId.get(feedFlowFileId) : null;
    }

    public Long getFeedFlowStartTime(ProvenanceEventRecord event) {
        String feedFlowFile = getFeedFlowFileId(event);
        if (feedFlowFile != null) {
            return feedFlowFileStartTime.getOrDefault(feedFlowFile, null);
        }
        return null;
    }

    public Long getFeedFlowEndTime(ProvenanceEventRecord event) {
        String feedFlowFile = getFeedFlowFileId(event);
        if (feedFlowFile != null) {
            return feedFlowFileEndTime.getOrDefault(feedFlowFile, null);
        }
        return null;
    }

    public Long getFeedFlowFileDuration(ProvenanceEventRecord event) {
        Long start = getFeedFlowStartTime(event);
        Long end = getFeedFlowEndTime(event);
        if (start != null && end != null) {
            return end - start;
        }
        return null;
    }

    private void clearMapsForEventFlowFile(String eventFlowFileId) {
        flowFileLastNonDropEventTime.remove(eventFlowFileId);
        allFlowFileToFeedFlowFile.remove(eventFlowFileId);
    }

    /**
     * Return the count of running feed flow files for a given starting processor id
     * @param feedProcessorId the starting feed processor id
     * @return the count of running feed flows
     */
    public Long getRunningFeedFlows(String feedProcessorId){
        return feedProcessorRunningFeedFlows.getOrDefault(feedProcessorId, new AtomicLong(0L)).get();
    }

    public Map<String,Long> getRunningFeedFlows(){
        return feedProcessorRunningFeedFlows.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }

    public Map<String,Long> getRunningFeedFlowsChanged(){
        return changedFeedProcessorRunningFeedFlows.stream().collect(Collectors.toMap(processorId -> processorId,processorId -> feedProcessorRunningFeedFlows.get(processorId).get()));
    }

    private void decrementRunningProcessorFeedFlows(String feedFlowFile){
        String feedProcessor = feedFlowFileIdToFeedProcessorId.get(feedFlowFile);
        if(feedProcessor != null){
            AtomicLong runningCount = feedProcessorRunningFeedFlows.get(feedProcessor);
            if( runningCount != null && runningCount.get() >=1) {
                runningCount.decrementAndGet();
                feedProcessorRunningFeedFlowsChanged.set(true);
                changedFeedProcessorRunningFeedFlows.add(feedProcessor);
            }
        }
    }

    private void clearMapsForFeedFlowFile(String feedFlowFile) {
        if (feedFlowFile != null) {
            detailedTrackingFeedFlowFileId.remove(feedFlowFile);
            feedFlowFileFailureCount.remove(feedFlowFile);
            feedFlowFileEndTime.remove(feedFlowFile);
            feedFlowFileStartTime.remove(feedFlowFile);
            feedFlowProcessing.remove(feedFlowFile);


            feedFlowFileIdToFeedProcessorId.remove(feedFlowFile);
        }
    }

    private void clearData(Long eventId, String eventFlowFileId) {
        String feedFlowFile = getFeedFlowFileId(eventFlowFileId);
        clearMapsForEventFlowFile(eventFlowFileId);
        if (isEndingFeedFlow(eventId)) {
            clearMapsForFeedFlowFile(feedFlowFile);
            eventsThatCompleteFeedFlow.remove(eventId);
        }
    }


    public void checkAndClear(String eventFlowFileId, String eventType, Long eventId) {
        if (ProvenanceEventType.DROP.name().equals(eventType)) {

            boolean isTrackingDetails = isTrackingDetails(eventFlowFileId);

            if (!isTrackingDetails) {
                //if we are not tracking ProvenanceEventDTO details then we can just expire all the flowfile data
                clearData(eventId, eventFlowFileId);
            } else {
                //if we are tracking details it needs to be added to an expiring map.
                //Sometimes the DROP event for the flowfile will come in before the next event causing us to loose the tracking information
                //this will happen in a very short time, so adding to an expiring cache to help manage the cleanup of these entries is needed.
                detailedTrackingFlowFilesToDelete.put(eventId, eventFlowFileId);
            }
        }

        eventDuration.remove(eventId);
        eventStartTime.remove(eventId);
    }


    /**
     * is this the last DROP event for a feed that is being tracked to go to ops manager
     *
     * @param event   the event
     * @param eventId the id
     * @return true if last event, false if not
     */
    public boolean beforeProcessingIsLastEventForTrackedFeed(ProvenanceEventRecord event, Long eventId) {
        String feedFlowFileId = allFlowFileToFeedFlowFile.get(event.getFlowFileUuid());
        if (isTrackingDetails(event.getFlowFileUuid()) && feedFlowFileId != null && ProvenanceEventType.DROP.equals(event.getEventType())) {
            //get the feed flow fileId for this event
            AtomicInteger activeCounts = feedFlowProcessing.get(feedFlowFileId);
            if (activeCounts != null) {
                return activeCounts.get() == 1;
            }
        }
        return false;
    }

    public void finishedEvent(ProvenanceEventRecord event, Long eventId) {

        String feedFlowFileId = allFlowFileToFeedFlowFile.get(event.getFlowFileUuid());
        if (feedFlowFileId != null && ProvenanceEventType.DROP.equals(event.getEventType())) {
            //get the feed flow fileId for this event
            AtomicInteger activeCounts = feedFlowProcessing.get(feedFlowFileId);
            if (activeCounts != null) {
                feedFlowProcessing.get(feedFlowFileId).decrementAndGet();
                if (activeCounts.get() <= 0) {
                    //Feed is finished
                    eventsThatCompleteFeedFlow.add(eventId);
                    feedFlowFileEndTime.put(feedFlowFileId, event.getEventTime());
                    decrementRunningProcessorFeedFlows(feedFlowFileId);
                }

            }

        }

        if (feedFlowFileId != null && ProvenanceEventUtil.isTerminatedByFailureRelationship(event)) {
            //add to failureMap
            feedFlowFileFailureCount.computeIfAbsent(feedFlowFileId, flowFileId -> new AtomicInteger(0)).incrementAndGet();
        }


    }

    public void cleanup(ProvenanceEventRecord event, Long eventId) {
        checkAndClear(event.getFlowFileUuid(), event.getEventType().name(), eventId);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FeedEventStatistics{");
        sb.append("detailedTrackingFeedFlowFileId=").append(detailedTrackingFeedFlowFileId.size());
        sb.append(", allFlowFileToFeedFlowFile=").append(allFlowFileToFeedFlowFile.size());
        sb.append(", feedFlowProcessing=").append(feedFlowProcessing.size());
        sb.append(", skippedEvents=").append(skippedEvents);
        sb.append('}');
        return sb.toString();
    }
}
