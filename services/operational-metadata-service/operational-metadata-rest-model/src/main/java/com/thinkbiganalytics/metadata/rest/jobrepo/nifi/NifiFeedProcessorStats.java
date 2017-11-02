package com.thinkbiganalytics.metadata.rest.jobrepo.nifi;

/*-
 * #%L
 * thinkbig-operational-metadata-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * A model classs to represent the stats of a feed
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NifiFeedProcessorStats {

    protected Long duration = 0L;
    protected DateTime minEventTime;
    protected DateTime maxEventTime;
    protected Long bytesIn = 0L;
    protected Long bytesOut = 0L;
    protected Long totalCount = 1L;
    protected Long failedCount = 0L;
    protected Long jobsStarted = 0L;
    protected Long jobsFinished = 0L;
    protected Long jobsFailed = 0L;
    protected Long jobDuration = 0L;
    protected Long successfulJobDuration = 0L;
    protected Long processorsFailed = 0L;
    protected Long flowFilesStarted = 0L;
    protected Long flowFilesFinished = 0L;
    protected Long maxEventId;
    private String id;
    private String feedName;
    private String processorId;
    private String processorName;
    private String feedProcessGroupId;
    private DateTime collectionTime;
    private String collectionId;
    private Long resultSetCount;

    private String latestFlowFileId;

    private String errorMessages;

    private DateTime errorMessageTimestamp;

    private BigDecimal jobsStartedPerSecond;

    private BigDecimal jobsFinishedPerSecond;

    private Long collectionIntervalSeconds;

    private Double timeInterval;

    public Double getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(Double timeInterval) {
        this.timeInterval = timeInterval;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    public String getFeedProcessGroupId() {
        return feedProcessGroupId;
    }

    public void setFeedProcessGroupId(String feedProcessGroupId) {
        this.feedProcessGroupId = feedProcessGroupId;
    }

    public DateTime getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(DateTime collectionTime) {
        this.collectionTime = collectionTime;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public DateTime getMinEventTime() {
        return minEventTime;
    }

    public void setMinEventTime(DateTime minEventTime) {
        this.minEventTime = minEventTime;
    }

    public DateTime getMaxEventTime() {
        return maxEventTime;
    }

    public void setMaxEventTime(DateTime maxEventTime) {
        this.maxEventTime = maxEventTime;
    }

    public Long getBytesIn() {
        return bytesIn;
    }

    public void setBytesIn(Long bytesIn) {
        this.bytesIn = bytesIn;
    }

    public Long getBytesOut() {
        return bytesOut;
    }

    public void setBytesOut(Long bytesOut) {
        this.bytesOut = bytesOut;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getJobsStarted() {
        return jobsStarted;
    }

    public void setJobsStarted(Long jobsStarted) {
        this.jobsStarted = jobsStarted;
    }

    public Long getJobsFinished() {
        return jobsFinished;
    }

    public void setJobsFinished(Long jobsFinished) {
        this.jobsFinished = jobsFinished;
    }

    public Long getJobsFailed() {
        return jobsFailed;
    }

    public void setJobsFailed(Long jobsFailed) {
        this.jobsFailed = jobsFailed;
    }

    public Long getJobDuration() {
        return jobDuration;
    }

    public void setJobDuration(Long jobDuration) {
        this.jobDuration = jobDuration;
    }

    public Long getSuccessfulJobDuration() {
        return successfulJobDuration;
    }

    public void setSuccessfulJobDuration(Long successfulJobDuration) {
        this.successfulJobDuration = successfulJobDuration;
    }

    public Long getProcessorsFailed() {
        return processorsFailed;
    }

    public void setProcessorsFailed(Long processorsFailed) {
        this.processorsFailed = processorsFailed;
    }

    public Long getFlowFilesStarted() {
        return flowFilesStarted;
    }

    public void setFlowFilesStarted(Long flowFilesStarted) {
        this.flowFilesStarted = flowFilesStarted;
    }

    public Long getFlowFilesFinished() {
        return flowFilesFinished;
    }

    public void setFlowFilesFinished(Long flowFilesFinished) {
        this.flowFilesFinished = flowFilesFinished;
    }

    public Long getResultSetCount() {
        return resultSetCount;
    }

    public void setResultSetCount(Long resultSetCount) {
        this.resultSetCount = resultSetCount;
    }

    public Long getMaxEventId() {
        return maxEventId;
    }

    public void setMaxEventId(Long maxEventId) {
        this.maxEventId = maxEventId;
    }

    public Long getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Long failedCount) {
        this.failedCount = failedCount;
    }

    public Long getCollectionIntervalSeconds() {
        return collectionIntervalSeconds;
    }
    public void setCollectionIntervalSeconds(Long collectionIntervalSeconds) {
        this.collectionIntervalSeconds = collectionIntervalSeconds;
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

    public String getLatestFlowFileId() {
        return latestFlowFileId;
    }

    public void setLatestFlowFileId(String latestFlowFileId) {
        this.latestFlowFileId = latestFlowFileId;
    }

    public String getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(String errorMessages) {
        this.errorMessages = errorMessages;
    }

    public DateTime getErrorMessageTimestamp() {
        return errorMessageTimestamp;
    }

    public void setErrorMessageTimestamp(DateTime errorMessageTimestamp) {
        this.errorMessageTimestamp = errorMessageTimestamp;
    }
}
