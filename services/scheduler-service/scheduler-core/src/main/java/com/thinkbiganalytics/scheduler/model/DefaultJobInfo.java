package com.thinkbiganalytics.scheduler.model;

/*-
 * #%L
 * thinkbig-scheduler-core
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
import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.JobInfo;
import com.thinkbiganalytics.scheduler.TriggerInfo;

import java.util.List;
import java.util.Map;

/**
 * JobInformation
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultJobInfo implements JobInfo {

    private JobIdentifier jobIdentifier;
    private List<TriggerInfo> triggers;
    private String description;
    private Class jobClass;
    @JsonIgnore
    private Map<String, Object> jobData;

    public DefaultJobInfo() {

    }

    public DefaultJobInfo(JobIdentifier jobIdentifier) {
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
