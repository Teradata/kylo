package com.thinkbiganalytics.scheduler.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.JobInfo;
import com.thinkbiganalytics.scheduler.TriggerInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 9/24/15.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class JobInfoImpl implements JobInfo {

    private JobIdentifier jobIdentifier;
    private List<TriggerInfo> triggers;
    private String description;
    private Class jobClass;
    private Map<String,Object> jobData;
    public JobInfoImpl() {

    }

    public JobInfoImpl(JobIdentifier jobIdentifier) {
        this.jobIdentifier = jobIdentifier;
    }

    @Override
    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    @Override
    public void setJobIdentifier(JobIdentifier jobIdentifier) {
        this.jobIdentifier = jobIdentifier;
    }

   
    @Override
    public List<TriggerInfo> getTriggers() {
        return triggers;
    }

    @Override
    public void setTriggers(List<TriggerInfo> triggers) {
        this.triggers = triggers;
    }

   
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

   
    @Override
    public Class getJobClass() {
        return jobClass;
    }

    @Override
    public void setJobClass(Class jobClass) {
        this.jobClass = jobClass;
    }

   
    @Override
    public Map<String, Object> getJobData() {
        return jobData;
    }

    @Override
    public void setJobData(Map<String, Object> jobData) {
        this.jobData = jobData;
    }
}
