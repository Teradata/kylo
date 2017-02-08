package com.thinkbiganalytics.scheduler.rest.model;

/*-
 * #%L
 * thinkbig-scheduler-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Hold information about a job
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobInfo {

    private ScheduleIdentifier jobIdentifier;
    private List<TriggerInfo> triggers;
    private String description;
    private Class jobClass;
    @JsonIgnore
    private Map<String, Object> jobData;

    public JobInfo() {

    }

    public JobInfo(ScheduleIdentifier jobIdentifier) {
        this.jobIdentifier = jobIdentifier;
    }


    public ScheduleIdentifier getJobIdentifier() {
        return jobIdentifier;
    }


    public void setJobIdentifier(ScheduleIdentifier jobIdentifier) {
        this.jobIdentifier = jobIdentifier;
    }


    public List<TriggerInfo> getTriggers() {
        return triggers;
    }


    public void setTriggers(List<TriggerInfo> triggers) {
        this.triggers = triggers;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public Class getJobClass() {
        return jobClass;
    }


    public void setJobClass(Class jobClass) {
        this.jobClass = jobClass;
    }


    public Map<String, Object> getJobData() {
        return jobData;
    }


    public void setJobData(Map<String, Object> jobData) {
        this.jobData = jobData;
    }
}
