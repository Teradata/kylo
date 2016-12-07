package com.thinkbiganalytics.metadata.jpa.jobrepo.step;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by sr186054 on 8/23/16.
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
                   + "join JpaNifiEvent as failedEvent on failedEvent.eventId = nifiEventStep.eventId "
                   + "and nifiEventStep.flowFileId = failedEvent.flowFileId "
                   + " and failedEvent.jobFlowFileId = :jobFlowFileId"
                   + " and failedEvent.isFailure = true "
                   + "where step.status <> 'FAILED'")
    List<JpaBatchStepExecution> findStepsInJobThatNeedToBeFailedByJobFlowFileId(@Param("jobFlowFileId") String jobFlowFileId);

    @Query(value = "select step from JpaBatchStepExecution as step "
                   + "join JpaNifiEventStepExecution as nifiEventStep on nifiEventStep.stepExecution.stepExecutionId = step.stepExecutionId  "
                   + "where nifiEventStep.eventId = :eventId and nifiEventStep.flowFileId = :flowFileId")
    JpaBatchStepExecution findByEventAndFlowFile(@Param("eventId") Long eventId, @Param("flowFileId") String flowFileId);


    @Query(value = "select step from JpaBatchStepExecution as step "
                   + "join JpaNifiEventStepExecution as nifiEventStep on nifiEventStep.stepExecution.stepExecutionId = step.stepExecutionId  "
                   + "where nifiEventStep.componentId = :componentId and nifiEventStep.jobFlowFileId = :flowFileId")
    JpaBatchStepExecution findByProcessorAndJobFlowFile(@Param("componentId") String processorId, @Param("flowFileId") String flowFileId);


    @Query(" select step from JpaBatchStepExecution as step where step.jobExecution.jobExecutionId = :jobExecutionId")
    List<JpaBatchStepExecution> findSteps(@Param("jobExecutionId") Long jobExecutionId);

}
