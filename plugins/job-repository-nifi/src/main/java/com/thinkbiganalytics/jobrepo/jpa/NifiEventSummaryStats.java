package com.thinkbiganalytics.jobrepo.jpa;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by sr186054 on 8/17/16.
 */

@Entity
@Table(name = "NIFI_EVENT_SUMMARY_STATS")
public class NifiEventSummaryStats {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
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

    @Column(name = "DURATION_MILLIS")
    protected Long duration = 0L;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "MIN_EVENT_TIME")
    protected DateTime minEventTime;


    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "MAX_EVENT_TIME")
    protected DateTime maxEventTime;

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





    public NifiEventSummaryStats(String feedName, String processorId) {
        this.feedName = feedName;
        this.processorId = processorId;
    }

    public NifiEventSummaryStats() {
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getFeedProcessGroupId() {
        return feedProcessGroupId;
    }

    public void setFeedProcessGroupId(String feedProcessGroupId) {
        this.feedProcessGroupId = feedProcessGroupId;
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

    public DateTime getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(DateTime collectionTime) {
        this.collectionTime = collectionTime;
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
}
