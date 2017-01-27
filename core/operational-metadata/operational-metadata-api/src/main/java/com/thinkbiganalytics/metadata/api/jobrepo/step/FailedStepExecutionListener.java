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
 * Listener called when a Step is marked as a Failure This is called before it is persisted. Listeners have the ability to modify/add data to the StepExecutionContext before it is persisted.
 */
public interface FailedStepExecutionListener {

    /**
     * called when a Step is marked as a Failure.   This is called before the step is persisted. Listeners have the ability to modify/add data to the StepExecutionContext before it is persisted.
     */
    void failedStep(BatchJobExecution jobExecution, BatchStepExecution stepExecution, String flowFileId, String componentId);

}
