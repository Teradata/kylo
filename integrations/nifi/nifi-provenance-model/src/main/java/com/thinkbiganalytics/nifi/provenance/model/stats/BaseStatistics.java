package com.thinkbiganalytics.nifi.provenance.model.stats;

/*-
 * #%L
 * thinkbig-nifi-provenance-model
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

/**
 */
public class BaseStatistics implements Serializable {

    protected Long time;
    protected long bytesIn = 0L;
    protected long bytesOut = 0L;
    protected long duration = 0L;
    protected long totalCount = 0L;
    protected long jobsStarted = 0L;
    protected long jobsFinished = 0L;
    protected long processorsFailed = 0L;
    protected long flowFilesStarted = 0L;
    protected long flowFilesFinished = 0L;
    protected long jobsFailed = 0L;
    protected long successfulJobDuration = 0L;
    protected long jobDuration = 0L;
    protected long maxEventId = 0L;

    protected String sourceConnectionIdentifier;

    protected String clusterNodeId;

    protected String clusterNodeAddress;

    public BaseStatistics(){

    }


    public BaseStatistics(BaseStatistics other) {
        this.time = other.time;
        this.bytesIn = other.bytesIn;
        this.bytesOut = other.bytesOut;
        this.duration = other.duration;
        this.totalCount = other.totalCount;
        this.jobsStarted = other.jobsStarted;
        this.jobsFinished = other.jobsFinished;
        this.processorsFailed = other.processorsFailed;
        this.flowFilesStarted = other.flowFilesStarted;
        this.flowFilesFinished = other.flowFilesFinished;
        this.jobsFailed = other.jobsFailed;
        this.successfulJobDuration = other.successfulJobDuration;
        this.jobDuration = other.jobDuration;
        this.maxEventId = other.maxEventId;
        this.clusterNodeId = other.clusterNodeId;
        this.clusterNodeAddress = other.clusterNodeAddress;
        this.sourceConnectionIdentifier = other.sourceConnectionIdentifier;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public long getBytesIn() {
        return bytesIn;
    }

    public void setBytesIn(long bytesIn) {
        this.bytesIn = bytesIn;
    }

    public long getBytesOut() {
        return bytesOut;
    }

    public void setBytesOut(long bytesOut) {
        this.bytesOut = bytesOut;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getJobsStarted() {
        return jobsStarted;
    }

    public void setJobsStarted(long jobsStarted) {
        this.jobsStarted = jobsStarted;
    }

    public long getJobsFinished() {
        return jobsFinished;
    }

    public void setJobsFinished(long jobsFinished) {
        this.jobsFinished = jobsFinished;
    }

    public long getProcessorsFailed() {
        return processorsFailed;
    }

    public void setProcessorsFailed(long processorsFailed) {
        this.processorsFailed = processorsFailed;
    }

    public long getFlowFilesStarted() {
        return flowFilesStarted;
    }

    public void setFlowFilesStarted(long flowFilesStarted) {
        this.flowFilesStarted = flowFilesStarted;
    }

    public long getFlowFilesFinished() {
        return flowFilesFinished;
    }

    public void setFlowFilesFinished(long flowFilesFinished) {
        this.flowFilesFinished = flowFilesFinished;
    }

    public long getJobsFailed() {
        return jobsFailed;
    }

    public void setJobsFailed(long jobsFailed) {
        this.jobsFailed = jobsFailed;
    }

    public long getSuccessfulJobDuration() {
        return successfulJobDuration;
    }

    public void setSuccessfulJobDuration(long successfulJobDuration) {
        this.successfulJobDuration = successfulJobDuration;
    }

    public long getJobDuration() {
        return jobDuration;
    }

    public void setJobDuration(long jobDuration) {
        this.jobDuration = jobDuration;
    }

    public long getMaxEventId() {
        return maxEventId;
    }

    public void setMaxEventId(long maxEventId) {
        this.maxEventId = maxEventId;
    }

    public String getClusterNodeId() {
        return clusterNodeId;
    }

    public void setClusterNodeId(String clusterNodeId) {
        this.clusterNodeId = clusterNodeId;
    }

    public String getClusterNodeAddress() {
        return clusterNodeAddress;
    }

    public void setClusterNodeAddress(String clusterNodeAddress) {
        this.clusterNodeAddress = clusterNodeAddress;
    }

    public void addBytesIn(Long bytesIn){
        this.bytesIn += nvl(bytesIn);
    }
    public void addBytesOut(Long bytesOut){
        this.bytesOut += nvl(bytesOut);
    }

    public void addDuration(Long duration){
        this.duration += nvl(duration);
    }

    public void addTotalCount(Long totalCount){
        this.totalCount += nvl(totalCount);
    }

    public void addJobsStarted( Long jobsStarted){
        this.jobsStarted += nvl(jobsStarted);
    }

    public void addJobsFinished(Long jobsFinished){
        this.jobsFinished += nvl(jobsFinished);
    }

    public void addSuccessfulJobDuration(Long successfulJobDuration){
        this.successfulJobDuration += nvl(successfulJobDuration);
    }

    public void addJobDuration(Long jobDuration){
        this.jobDuration += nvl(jobDuration);
    }

    public void addProcessorsFailed(Long processorsFailed){
        this.processorsFailed += nvl(processorsFailed);
    }

    public void addJobsFailed(Long jobsFailed){
        this.jobsFailed += nvl(jobsFailed);
    }

    private Long nvl(Long item, Long nullValue){
        return item == null ? nullValue : item;
    }
    private Long nvl(Long item){
        return nvl(item,0L);
    }

    public String getSourceConnectionIdentifier() {
        if(sourceConnectionIdentifier == null){
            sourceConnectionIdentifier = GroupedStats.DEFAULT_SOURCE_CONNECTION_ID;
        }
        return sourceConnectionIdentifier;
    }

    public void setSourceConnectionIdentifier(String sourceConnectionIdentifier) {
        this.sourceConnectionIdentifier = sourceConnectionIdentifier;
    }

    public void clear() {
        this.time = null;
        this.bytesIn = 0L;
        this.bytesOut = 0L;
        this.duration = 0L;
        this.totalCount = 0L;
        this.jobsStarted = 0L;
        this.jobsFinished = 0L;
        this.processorsFailed = 0L;
        this.flowFilesStarted = 0L;
        this.flowFilesFinished = 0L;
        this.jobsFailed = 0L;
        this.successfulJobDuration = 0L;
        this.jobDuration = 0L;
        this.maxEventId = 0L;
        this.clusterNodeId = null;
        this.clusterNodeAddress = null;
        this.sourceConnectionIdentifier = null;
    }
}
