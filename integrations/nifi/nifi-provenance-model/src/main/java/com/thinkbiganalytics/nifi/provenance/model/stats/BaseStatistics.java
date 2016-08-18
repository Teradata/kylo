package com.thinkbiganalytics.nifi.provenance.model.stats;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 8/16/16.
 */
public class BaseStatistics {

    protected DateTime time;
    protected Long bytesIn = 0L;
    protected Long bytesOut = 0L;
    protected Long duration = 0L;
    protected Long totalCount = 1L;
    protected Long jobsStarted = 0L;
    protected Long jobsFinished = 0L;
    protected Long processorsFailed = 0L;
    protected Long flowFilesStarted = 0L;
    protected Long flowFilesFinished = 0L;


    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public Long getBytesIn() {
        return bytesIn;
    }

    public Long getBytesOut() {
        return bytesOut;
    }

    public Long getDuration() {
        return duration;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public Long getJobsStarted() {
        return jobsStarted;
    }

    public Long getJobsFinished() {
        return jobsFinished;
    }

    public Long getProcessorsFailed() {
        return processorsFailed;
    }

    public Long getFlowFilesStarted() {
        return flowFilesStarted;
    }

    public Long getFlowFilesFinished() {
        return flowFilesFinished;
    }


    public Double average(Long stat) {
        return stat.doubleValue() / totalCount.doubleValue();
    }

    public Double getAvgDuration() {
        return average(duration);
    }


    public void setBytesIn(Long bytesIn) {
        this.bytesIn = bytesIn;
    }

    public void setBytesOut(Long bytesOut) {
        this.bytesOut = bytesOut;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public void setJobsStarted(Long jobsStarted) {
        this.jobsStarted = jobsStarted;
    }

    public void setJobsFinished(Long jobsFinished) {
        this.jobsFinished = jobsFinished;
    }

    public void setProcessorsFailed(Long processorsFailed) {
        this.processorsFailed = processorsFailed;
    }

    public void setFlowFilesStarted(Long flowFilesStarted) {
        this.flowFilesStarted = flowFilesStarted;
    }

    public void setFlowFilesFinished(Long flowFilesFinished) {
        this.flowFilesFinished = flowFilesFinished;
    }
}
