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
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import java.util.List;

/**
 * Provider for accessing {@link BatchStepExecution} data
 */
public interface BatchStepExecutionProvider {


    /**
     * Update a StepExecution record
     */
    BatchStepExecution update(BatchStepExecution stepExecution);

    /**
     * Create a new StepExecution record  from a Provenance Event Record
     */
    BatchStepExecution createStepExecution(BatchJobExecution jobExecution, ProvenanceEventRecordDTO event);

    /**
     * When a step fails, get notified of the failure
     */
    void subscribeToFailedSteps(FailedStepExecutionListener listener);


    /**
     * Return all the step executions for a given job execution id
     *
     * @return the steps pertaining to a given job
     */
    List<? extends BatchStepExecution> getSteps(Long jobExecutionId);

}
