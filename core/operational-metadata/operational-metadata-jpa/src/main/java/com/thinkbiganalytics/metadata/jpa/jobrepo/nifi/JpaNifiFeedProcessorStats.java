package com.thinkbiganalytics.metadata.jpa.jobrepo.nifi;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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

import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorErrors;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStats;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Aggregated statistics gathered from NiFi for a feed and processor over a given time interval
 */
@Entity
@Table(name = "NIFI_FEED_PROCESSOR_STATS")
public class JpaNifiFeedProcessorStats implements NifiFeedProcessorStats, NifiFeedProcessorErrors {

    @Column(name = "DURATION_MILLIS")
    protected Long duration = 0L;
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "MIN_EVENT_TIME")
    protected DateTime minEventTime;
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "MAX_EVENT_TIME")
    protected DateTime maxEventTime;

    @Transient
    private Long minEventTimeMillis;

    @Column(name = "COLLECTION_INTERVAL_SEC")
    protected Long collectionIntervalSeconds = null;

    @Column(name = "BYTES_IN")
    protected Long bytesIn = 0L;
    @Column(name = "BYTES_OUT")
    protected Long bytesOut = 0L;
    @Column(name = "TOTAL_EVENTS")
    protected Long totalCount = 1L;
    @Column(name = "JOBS_STARTED")
    protected Long jobsStarted = 0L;
    @Column(name = "JOBS_FINISHED")
    protected Long jobsFinished = 0L;
    @Column(name = "JOBS_FAILED")
    protected Long jobsFailed = 0L;
    @Column(name = "JOB_DURATION")
    protected Long jobDuration = 0L;
    @Column(name = "SUCCESSFUL_JOB_DURATION")
    protected Long successfulJobDuration = 0L;
    @Column(name = "PROCESSORS_FAILED")
    protected Long processorsFailed = 0L;
    @Column(name = "FLOW_FILES_STARTED")
    protected Long flowFilesStarted = 0L;
    @Column(name = "FLOW_FILES_FINISHED")
    protected Long flowFilesFinished = 0L;
    @Column(name = "MAX_EVENT_ID")
    protected Long maxEventId = 0L;
    @Column(name = "CLUSTER_NODE_ID")
    protected String clusterNodeId;
    @Column(name = "CLUSTER_NODE_ADDRESS")
    protected String clusterNodeAddress;

    @Column(name = "FAILED_EVENTS")
    protected Long failedCount = 0L;

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", unique = true)
    private String id;
    @Column(name = "FM_FEED_NAME")
    private String feedName;
    @Column(name = "NIFI_PROCESSOR_ID")
    private String processorId;
    @Column(name = "PROCESSOR_NAME")
    private String processorName;
    @Column(name = "NIFI_FEED_PROCESS_GROUP_ID")
    private String feedProcessGroupId;
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "COLLECTION_TIME")
    private DateTime collectionTime;
    @Column(name = "COLLECTION_ID")
    private String collectionId;

    @Column(name = "LATEST_FLOW_FILE_ID")
    private String latestFlowFileId;

    @Column(name = "ERROR_MESSAGES")
    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "4000")})
    private String errorMessages;

    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "ERROR_MESSAGES_TIMESTAMP")
    @QueryType(PropertyType.COMPARABLE)
    private DateTime errorMessageTimestamp;

    @Transient
    private BigDecimal jobsStartedPerSecond;

    @Transient
    private BigDecimal jobsFinishedPerSecond;

    @Transient
    private Double timeInterval;

    @Transient
    private Double minEventTimeGroup;

    public JpaNifiFeedProcessorStats(String feedName, String processorId) {
        this.feedName = feedName;
        this.processorId = processorId;
    }

    public JpaNifiFeedProcessorStats() {
    }

    @Override
    public String getProcessorName() {
        return processorName;
    }

    @Override
    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    @Override
    public String getFeedName() {
        return feedName;
    }

    @Override
    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    @Override
    public String getProcessorId() {
        return processorId;
    }

    @Override
    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }


    @Override
    public String getFeedProcessGroupId() {
        return feedProcessGroupId;
    }

    @Override
    public void setFeedProcessGroupId(String feedProcessGroupId) {
        this.feedProcessGroupId = feedProcessGroupId;
    }


    @Override
    public String getCollectionId() {
        return collectionId;
    }

    @Override
    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    @Override
    public Long getDuration() {
        return duration;
    }

    @Override
    public void setDuration(Long duration) {
        this.duration = duration;
    }


    @Override
    public Long getBytesIn() {
        return bytesIn;
    }

    @Override
    public void setBytesIn(Long bytesIn) {
        this.bytesIn = bytesIn;
    }

    @Override
    public Long getBytesOut() {
        return bytesOut;
    }

    @Override
    public void setBytesOut(Long bytesOut) {
        this.bytesOut = bytesOut;
    }

    @Override
    public Long getTotalCount() {
        return totalCount;
    }

    @Override
    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public Long getFailedCount() {
        return failedCount;
    }

    @Override
    public void setFailedCount(Long failedCount) {
        this.failedCount = failedCount;
    }

    @Override
    public Long getJobsStarted() {
        return jobsStarted;
    }

    @Override
    public void setJobsStarted(Long jobsStarted) {
        this.jobsStarted = jobsStarted;
    }

    @Override
    public Long getJobsFinished() {
        return jobsFinished;
    }

    @Override
    public void setJobsFinished(Long jobsFinished) {
        this.jobsFinished = jobsFinished;
    }

    @Override
    public Long getJobsFailed() {
        return jobsFailed;
    }

    @Override
    public void setJobsFailed(Long jobsFailed) {
        this.jobsFailed = jobsFailed;
    }

    @Override
    public Long getJobDuration() {
        return jobDuration;
    }

    @Override
    public void setJobDuration(Long jobDuration) {
        this.jobDuration = jobDuration;
    }

    @Override
    public Long getSuccessfulJobDuration() {
        return successfulJobDuration;
    }

    @Override
    public void setSuccessfulJobDuration(Long successfulJobDuration) {
        this.successfulJobDuration = successfulJobDuration;
    }

    @Override
    public Long getProcessorsFailed() {
        return processorsFailed;
    }

    @Override
    public void setProcessorsFailed(Long processorsFailed) {
        this.processorsFailed = processorsFailed;
    }

    @Override
    public Long getFlowFilesStarted() {
        return flowFilesStarted;
    }

    @Override
    public void setFlowFilesStarted(Long flowFilesStarted) {
        this.flowFilesStarted = flowFilesStarted;
    }

    @Override
    public Long getFlowFilesFinished() {
        return flowFilesFinished;
    }

    @Override
    public void setFlowFilesFinished(Long flowFilesFinished) {
        this.flowFilesFinished = flowFilesFinished;
    }

    @Override
    public DateTime getCollectionTime() {
        return collectionTime;
    }

    @Override
    public void setCollectionTime(DateTime collectionTime) {
        this.collectionTime = collectionTime;
    }

    @Override
    public DateTime getMinEventTime() {
        return minEventTime;
    }

    @Override
    public void setMinEventTime(DateTime minEventTime) {
        this.minEventTime = minEventTime;
        setMinEventTimeMillis(minEventTime.getMillis());
    }

    public Long getMinEventTimeMillis() {
        return minEventTimeMillis;
    }

    public void setMinEventTimeMillis(Long minEventTimeMillis) {
        this.minEventTimeMillis = minEventTimeMillis;
    }

    @Override
    public DateTime getMaxEventTime() {
        return maxEventTime;
    }

    @Override
    public void setMaxEventTime(DateTime maxEventTime) {
        this.maxEventTime = maxEventTime;
    }

    @Override
    public Long getMaxEventId() {
        return maxEventId;
    }

    @Override
    public void setMaxEventId(Long maxEventId) {
        this.maxEventId = maxEventId;
    }

    @Override
    public String getClusterNodeId() {
        return clusterNodeId;
    }

    @Override
    public void setClusterNodeId(String clusterNodeId) {
        this.clusterNodeId = clusterNodeId;
    }

    @Override
    public String getClusterNodeAddress() {
        return clusterNodeAddress;
    }

    @Override
    public void setClusterNodeAddress(String clusterNodeAddress) {
        this.clusterNodeAddress = clusterNodeAddress;
    }

    @Override
    public Long getCollectionIntervalSeconds() {
        return collectionIntervalSeconds;
    }

    @Override
    public void setCollectionIntervalSeconds(Long collectionIntervalSeconds) {
        this.collectionIntervalSeconds = collectionIntervalSeconds;
    }

    public String getLatestFlowFileId() {
        return latestFlowFileId;
    }

    public void setLatestFlowFileId(String latestFlowFileId) {
        this.latestFlowFileId = latestFlowFileId;
    }

    @Override
    public String getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(String errorMessages) {
        this.errorMessages = errorMessages;
    }

    @Override
    public DateTime getErrorMessageTimestamp() {
        return errorMessageTimestamp;
    }

    public void setErrorMessageTimestamp(DateTime errorMessageTimestamp) {
        this.errorMessageTimestamp = errorMessageTimestamp;
    }

    public BigDecimal getJobsStartedPerSecond() {
        return jobsStartedPerSecond;
    }

    public void setJobsStartedPerSecond(BigDecimal jobsStartedPerSecond) {
        this.jobsStartedPerSecond = jobsStartedPerSecond;
    }

    public BigDecimal getJobsFinishedPerSecond() {
        return jobsFinishedPerSecond;
    }

    public void setJobsFinishedPerSecond(BigDecimal jobsFinishedPerSecond) {
        this.jobsFinishedPerSecond = jobsFinishedPerSecond;
    }

    public Double getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(Double timeInterval) {
        this.timeInterval = timeInterval;
    }

    public Double getMinEventTimeGroup() {
        return minEventTimeGroup;
    }

    public void setMinEventTimeGroup(Double minEventTimeGroup) {
        this.minEventTimeGroup = minEventTimeGroup;
    }
}
