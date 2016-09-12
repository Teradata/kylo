package com.thinkbiganalytics.jobrepo.jpa;

import com.thinkbiganalytics.jobrepo.jpa.model.NifiStepExecution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface NifiStepExecutionRepository extends JpaRepository<NifiStepExecution, Long> {


    @Query(value = "select step from NifiStepExecution as step "
                   + "join NifiEventStepExecution as nifiEventStep on nifiEventStep.stepExecution.stepExecutionId = step.stepExecutionId  "
                   + "join NifiFailedEvent failedEvent on failedEvent.eventId = nifiEventStep.eventId "
                   + "and nifiEventStep.flowFileId = failedEvent.flowFileId "
                   + "where step.jobExecution.jobExecutionId = :jobExecutionId AND step.status <> 'FAILED'")
    List<NifiStepExecution> findStepsInJobThatNeedToBeFailed(@Param("jobExecutionId") Long jobExecutionId);


    @Query(value = "select step from NifiStepExecution as step "
                   + "join NifiEventStepExecution as nifiEventStep on nifiEventStep.stepExecution.stepExecutionId = step.stepExecutionId  "
                   + "where nifiEventStep.eventId = :eventId and nifiEventStep.flowFileId = :flowFileId")
    NifiStepExecution findByEventAndFlowFile(@Param("eventId") Long eventId, @Param("flowFileId") String flowFileId);


    @Query(value = "select step from NifiStepExecution as step "
                   + "join NifiEventStepExecution as nifiEventStep on nifiEventStep.stepExecution.stepExecutionId = step.stepExecutionId  "
                   + "where nifiEventStep.componentId = :componentId and nifiEventStep.jobFlowFileId = :flowFileId")
    NifiStepExecution findByProcessorAndJobFlowFile(@Param("componentId") String processorId, @Param("flowFileId") String flowFileId);

}
