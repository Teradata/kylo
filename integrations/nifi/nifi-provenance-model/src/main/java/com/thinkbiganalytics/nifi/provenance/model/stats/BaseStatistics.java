package com.thinkbiganalytics.nifi.provenance.model.stats;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by sr186054 on 8/16/16.
 */
public class BaseStatistics implements Serializable {

    protected DateTime time;
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


    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
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
}
