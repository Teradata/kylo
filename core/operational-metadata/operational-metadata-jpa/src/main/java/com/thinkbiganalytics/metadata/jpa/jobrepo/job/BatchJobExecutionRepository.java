package com.thinkbiganalytics.metadata.jpa.jobrepo.job;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface BatchJobExecutionRepository extends JpaRepository<JpaBatchJobExecution, Long>, QueryDslPredicateExecutor<Long> {


    @Query(value = "select job from JpaBatchJobExecution as job "
                   + "join JpaNifiEventJobExecution as nifiEventJob on nifiEventJob.jobExecution.jobExecutionId = job.jobExecutionId  "
                   + "where nifiEventJob.eventId = :eventId and nifiEventJob.flowFileId = :flowFileId")
    JpaBatchJobExecution findByEventAndFlowFile(@Param("eventId") Long eventId, @Param("flowFileId") String flowFileId);

    //

    @Query(value = "  select job from JpaBatchJobExecution as job "
                   + "join JpaNifiEventJobExecution as nifiEventJob on nifiEventJob.jobExecution.jobExecutionId = job.jobExecutionId "
                   + "where nifiEventJob.flowFileId in ( "
                   + " select relatedJobs.flowFileId "
                   + " from JpaNifiRelatedRootFlowFiles relatedJobs "
                   + " where relatedJobs.relationId in ( "
                   + "   select related.relationId from JpaNifiRelatedRootFlowFiles related "
                   + "   inner join JpaNifiEventJobExecution as nifiEventJob on nifiEventJob.flowFileId = related.flowFileId  "
                   + " AND nifiEventJob.jobExecution.jobExecutionId = :jobExecutionId)"
                   + ")")
    List<JpaBatchJobExecution> findRelatedJobExecutions(@Param("jobExecutionId") Long jobExecutionId);

    @Query(value = "  select job from JpaBatchJobExecution as job "
                   + "join JpaNifiEventJobExecution as nifiEventJob on nifiEventJob.jobExecution.jobExecutionId = job.jobExecutionId "
                   + "where nifiEventJob.flowFileId in ( "
                   + " select relatedJobs.flowFileId "
                   + " from JpaNifiRelatedRootFlowFiles relatedJobs "
                   + " where relatedJobs.relationId in ( "
                   + "   select related.relationId from JpaNifiRelatedRootFlowFiles related "
                   + "   inner join JpaNifiEventJobExecution as nifiEventJob on nifiEventJob.flowFileId = related.flowFileId  "
                   + " AND nifiEventJob.jobExecution.jobExecutionId = :jobExecutionId)"
                   + ")"
                   + "and job.endTime is null")
    List<JpaBatchJobExecution> findRunningRelatedJobExecutions(@Param("jobExecutionId") Long jobExecutionId);


    @Query(value = "  select CASE WHEN count(job) >0 then true else false END "
                   + "from JpaBatchJobExecution as job "
                   + "join JpaNifiEventJobExecution as nifiEventJob on nifiEventJob.jobExecution.jobExecutionId = job.jobExecutionId "
                   + "where nifiEventJob.flowFileId in ( "
                   + " select relatedJobs.flowFileId "
                   + " from JpaNifiRelatedRootFlowFiles relatedJobs "
                   + " where relatedJobs.relationId in ( "
                   + "   select related.relationId from JpaNifiRelatedRootFlowFiles related "
                   + "   inner join JpaNifiEventJobExecution as nifiEventJob on nifiEventJob.flowFileId = related.flowFileId  "
                   + " AND nifiEventJob.jobExecution.jobExecutionId = :jobExecutionId)"
                   + ")"
                   + "and job.endTime is null")
    Boolean hasRunningRelatedJobs(@Param("jobExecutionId") Long jobExecutionId);

    @Query(value = "  select CASE WHEN count(job) >0 then true else false END "
                   + "from JpaBatchJobExecution as job "
                   + "join JpaNifiEventJobExecution as nifiEventJob on nifiEventJob.jobExecution.jobExecutionId = job.jobExecutionId "
                   + "where nifiEventJob.flowFileId in ( "
                   + " select relatedJobs.flowFileId "
                   + " from JpaNifiRelatedRootFlowFiles relatedJobs "
                   + " where relatedJobs.relationId in ( "
                   + "   select related.relationId from JpaNifiRelatedRootFlowFiles related "
                   + "   inner join JpaNifiEventJobExecution as nifiEventJob on nifiEventJob.flowFileId = related.flowFileId  "
                   + " AND nifiEventJob.jobExecution.jobExecutionId = :jobExecutionId)"
                   + ")")
    Boolean hasRelatedJobs(@Param("jobExecutionId") Long jobExecutionId);


    @Query(value = "  select CASE WHEN count(job) >0 then true else false END "
                   + "from JpaBatchJobExecution as job "
                   + "join JpaNifiEventJobExecution as nifiEventJob on nifiEventJob.jobExecution.jobExecutionId = job.jobExecutionId "
                   + "where nifiEventJob.flowFileId in ( "
                   + " select relatedJobs.flowFileId "
                   + " from JpaNifiRelatedRootFlowFiles relatedJobs "
                   + " where relatedJobs.relationId in ( "
                   + "   select related.relationId from JpaNifiRelatedRootFlowFiles related "
                   + "   inner join JpaNifiEventJobExecution as nifiEventJob on nifiEventJob.flowFileId = related.flowFileId  "
                   + " AND nifiEventJob.jobExecution.jobExecutionId = :jobExecutionId)"
                   + ")"
                   + "and job.status = 'FAILED'")
    Boolean hasRelatedJobFailures(@Param("jobExecutionId") Long jobExecutionId);

}
