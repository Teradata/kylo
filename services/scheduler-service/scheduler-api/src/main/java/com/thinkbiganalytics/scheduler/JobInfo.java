package com.thinkbiganalytics.scheduler;

/*-
 * #%L
 * thinkbig-scheduler-api
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

import java.util.List;
import java.util.Map;

/**
 * Return information about a given scheduled job
 */
public interface JobInfo {

    /**
     * Return the job identifier
     *
     * @return the identifier for the scheduled job
     */
    JobIdentifier getJobIdentifier();

    /**
     * set the job identifier
     *
     * @param jobIdentifier the identifier for the scheduled job
     */
    void setJobIdentifier(JobIdentifier jobIdentifier);

    /**
     * Return the list of triggers associated with the job
     *
     * @return the list of triggers associated with the job
     */
    List<TriggerInfo> getTriggers();

    /**
     * set the triggers on this job
     *
     * @param triggers the triggers associated with th job
     */
    void setTriggers(List<TriggerInfo> triggers);

    /**
     * Return a description about the job
     *
     * @return a description about the job
     */
    String getDescription();

    /**
     * set a description about the job
     *
     * @param description a description about the job
     */
    void setDescription(String description);

    /**
     * Return the class associated with this job
     *
     * @return the class associated with this job
     */
    Class getJobClass();

    /**
     * set the class associated with this job
     *
     * @param jobClass the class associated with this job
     */
    void setJobClass(Class jobClass);

    /**
     * Return any additional data for this job
     *
     * @return any additional data for this job
     */
    Map<String, Object> getJobData();

    /**
     * set any additional data for this job
     *
     * @param jobData any additional data for this job
     */
    void setJobData(Map<String, Object> jobData);
}
