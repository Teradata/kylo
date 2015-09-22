package com.thinkbiganalytics.scheduler.quartz.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.quartz.*;

/**
 * Created by sr186054 on 9/20/15.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class JobDetailDTO {

    private String name;
    private String group;
    private JobKey jobKey;
    private String description;
    private Class<? extends Job> jobClass;
    @JsonIgnore
    private JobDataMap jobDataMap;
    private boolean durability;
    private boolean shouldRecover;
    private transient JobKey key;


    public JobDetailDTO(String name, String group, JobKey jobKey, String description, Class<? extends Job> jobClass, JobDataMap jobDataMap, boolean durability, boolean shouldRecover, JobKey key, boolean isPersistJobDataAfterExecution, boolean isConcurrentExectionDisallowed, boolean requestsRecovery) {

    }

    public JobDetailDTO(JobDetail jobDetail) {
        this.name = jobDetail.getKey().getName();
        this.group = jobDetail.getKey().getGroup();
        this.jobKey = jobDetail.getKey();
        this.description = jobDetail.getDescription();
        this.jobClass = jobDetail.getJobClass();
        this.jobDataMap = jobDetail.getJobDataMap();
        this.durability = jobDetail.isDurable();
        this.shouldRecover = jobDetail.requestsRecovery();
        this.isPersistJobDataAfterExecution = jobDetail.isPersistJobDataAfterExecution();
        this.isConcurrentExectionDisallowed = jobDetail.isConcurrentExectionDisallowed();
        this.requestsRecovery = jobDetail.requestsRecovery();
    }


    boolean isPersistJobDataAfterExecution;

    boolean isConcurrentExectionDisallowed;

    boolean requestsRecovery;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public JobKey getJobKey() {
        return jobKey;
    }

    public void setJobKey(JobKey jobKey) {
        this.jobKey = jobKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Class<? extends Job> getJobClass() {
        return jobClass;
    }

    public void setJobClass(Class<? extends Job> jobClass) {
        this.jobClass = jobClass;
    }

    public JobDataMap getJobDataMap() {
        return jobDataMap;
    }

    public void setJobDataMap(JobDataMap jobDataMap) {
        this.jobDataMap = jobDataMap;
    }

    public boolean isDurability() {
        return durability;
    }

    public void setDurability(boolean durability) {
        this.durability = durability;
    }

    public boolean isShouldRecover() {
        return shouldRecover;
    }

    public void setShouldRecover(boolean shouldRecover) {
        this.shouldRecover = shouldRecover;
    }

    public JobKey getKey() {
        return key;
    }

    public void setKey(JobKey key) {
        this.key = key;
    }

    public boolean isPersistJobDataAfterExecution() {
        return isPersistJobDataAfterExecution;
    }

    public void setIsPersistJobDataAfterExecution(boolean isPersistJobDataAfterExecution) {
        this.isPersistJobDataAfterExecution = isPersistJobDataAfterExecution;
    }

    public boolean isConcurrentExectionDisallowed() {
        return isConcurrentExectionDisallowed;
    }

    public void setIsConcurrentExectionDisallowed(boolean isConcurrentExectionDisallowed) {
        this.isConcurrentExectionDisallowed = isConcurrentExectionDisallowed;
    }

    public boolean isRequestsRecovery() {
        return requestsRecovery;
    }

    public void setRequestsRecovery(boolean requestsRecovery) {
        this.requestsRecovery = requestsRecovery;
    }
}
