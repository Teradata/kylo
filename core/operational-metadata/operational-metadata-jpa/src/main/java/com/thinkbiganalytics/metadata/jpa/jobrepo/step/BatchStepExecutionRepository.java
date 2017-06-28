package com.thinkbiganalytics.metadata.jpa.jobrepo.step;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring data repository for {@link JpaBatchStepExecution}
 */
public interface BatchStepExecutionRepository extends JpaRepository<JpaBatchStepExecution, Long> {

    @Query(value = "select step from JpaBatchStepExecution as step "
                   + "join JpaNifiEventStepExecution as nifiEventStep on nifiEventStep.stepExecution.stepExecutionId = step.stepExecutionId  "
                   + "join JpaNifiEvent as failedEvent on failedEvent.eventId = nifiEventStep.eventId "
                   + "and nifiEventStep.flowFileId = failedEvent.flowFileId "
                   + "and failedEvent.isFailure = true "
                   + "where step.jobExecution.jobExecutionId = :jobExecutionId AND step.status <> 'FAILED'")
    List<JpaBatchStepExecution> findStepsInJobThatNeedToBeFailed(@Param("jobExecutionId") Long jobExecutionId);

    @Query(value = "select step from JpaBatchStepExecution as step "
                   + "join JpaNifiEventStepExecution as nifiEventStep on nifiEventStep.stepExecution.stepExecutionId = step.stepExecutionId  "
                   + "where nifiEventStep.componentId = :componentId and nifiEventStep.jobFlowFileId = :flowFileId")
    JpaBatchStepExecution findByProcessorAndJobFlowFile(@Param("componentId") String processorId, @Param("flowFileId") String flowFileId);
}
