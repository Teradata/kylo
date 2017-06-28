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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Backup of the FeedEventStatistics.  This is the object that will be serialized to disk upon NiFi shutdown
 */
public class FeedEventStatisticsData implements Serializable {

    protected Map<String, String> feedFlowFileIdToFeedProcessorId = new ConcurrentHashMap<>();

    ////Track feedFlowFile relationships to parent/child flow files for lifetime feed job execution
    protected Set<String> detailedTrackingFeedFlowFileId = new HashSet<>();

    /**
     * Map of all the flow files as they pertain to the starting feed flow file
     * Used to expire EventStatistics
     */
    protected Map<String, String> allFlowFileToFeedFlowFile = new ConcurrentHashMap<>();

    /**
     *  feed flow file to respective flow files that are detail tracked.
     */
    protected Map<String, Set<String>> detailedTrackingInverseInverseMap = new ConcurrentHashMap<>();

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
    Map<String, AtomicInteger> feedFlowFileFailureCount = new ConcurrentHashMap<>();

    public FeedEventStatisticsData() {

    }


    public FeedEventStatisticsData(FeedEventStatistics other) {
        this.feedFlowFileIdToFeedProcessorId = other.feedFlowFileIdToFeedProcessorId;
        this.detailedTrackingFeedFlowFileId = other.detailedTrackingFeedFlowFileId;
        this.allFlowFileToFeedFlowFile = other.allFlowFileToFeedFlowFile;
        this.flowFileLastNonDropEventTime = other.flowFileLastNonDropEventTime;
        this.eventDuration = other.eventDuration;
        this.eventStartTime = other.eventStartTime;
        this.feedFlowFileStartTime = other.feedFlowFileStartTime;
        this.feedFlowFileEndTime = other.feedFlowFileEndTime;
        this.eventsThatCompleteFeedFlow = other.eventsThatCompleteFeedFlow;
        this.feedFlowProcessing = other.feedFlowProcessing;
        this.skippedEvents = other.skippedEvents;
        this.feedFlowFileFailureCount = other.feedFlowFileFailureCount;
    }

    public void load() {

    }

}
