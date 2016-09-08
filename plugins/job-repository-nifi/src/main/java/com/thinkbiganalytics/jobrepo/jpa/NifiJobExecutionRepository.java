package com.thinkbiganalytics.jobrepo.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface NifiJobExecutionRepository extends JpaRepository<NifiJobExecution, Long>, QueryDslPredicateExecutor<Long> {


    @Query(value = "select job from NifiJobExecution as job "
                   + "join NifiEventJobExecution as nifiEventJob on nifiEventJob.jobExecution.jobExecutionId = job.jobExecutionId  "
                   + "where nifiEventJob.eventId = :eventId and nifiEventJob.flowFileId = :flowFileId")
    NifiJobExecution findByEventAndFlowFile(@Param("eventId") Long eventId, @Param("flowFileId") String flowFileId);


}
