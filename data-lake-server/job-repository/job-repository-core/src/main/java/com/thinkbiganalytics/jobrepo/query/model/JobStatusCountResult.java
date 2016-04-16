package com.thinkbiganalytics.jobrepo.query.model;

import java.util.Date;

/**
 * Created by sr186054 on 9/2/15.
 */
public class JobStatusCountResult implements JobStatusCount {

    private String feedName;
    private String jobName;
    private String status;
    private Date date;
    private Long count;

    public JobStatusCountResult(){

    }

    public JobStatusCountResult(JobStatusCount jobStatusCount){
        this.feedName = jobStatusCount.getFeedName();
        this.jobName = jobStatusCount.getJobName();
        this.status = jobStatusCount.getStatus();
        this.date = jobStatusCount.getDate();
        this.count = jobStatusCount.getCount();
    }

    @Override
    public Long getCount() {
        return count;
    }

    @Override
    public void setCount(Long count) {
        this.count = count;
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
    public String getJobName() {
        return jobName;
    }

    @Override
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }


}
