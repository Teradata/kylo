package com.thinkbiganalytics.metadata.api.jobrepo.step;

/*-
 * #%L
 * thinkbig-operational-metadata-api
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

import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;

/**
 * The value of some context attribute captured during the execution of a job and step
 */
public interface BatchStepExecutionContextValue {

    /**
     * Return the step this context value belongs to
     *
     * @return the step this context value belongs to
     */
    BatchStepExecution getStepExecution();

    /**
     * Return the name of this attribute
     *
     * @return the name of this attribute
     */
    String getKeyName();

    /**
     * Return a unique id representing this value
     *
     * @return a unique id representing this value
     */
    String getId();

    /**
     * Return the job execution id {@link BatchJobExecution#getJobExecutionId()}
     *
     * @return the job execution id
     */
    Long getJobExecutionId();

    /**
     * return the actual value of this key
     */
    String getStringVal();
}
