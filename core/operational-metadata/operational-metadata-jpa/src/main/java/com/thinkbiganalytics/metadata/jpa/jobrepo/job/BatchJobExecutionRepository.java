package com.thinkbiganalytics.metadata.jpa.jobrepo.job;

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


import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.jpa.feed.RepositoryType;
import com.thinkbiganalytics.metadata.jpa.feed.security.FeedOpsAccessControlRepository;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Spring data repository for accessing {@link JpaBatchJobExecution}
 */
@RepositoryType(BatchJobExecutionSecuringRepository.class)
public interface BatchJobExecutionRepository extends JpaRepository<JpaBatchJobExecution, Long>, QueryDslPredicateExecutor<JpaBatchJobExecution> {

    @Query(value = "select distinct job from JpaBatchJobExecution as job "
                   + "join JpaNifiEventJobExecution as nifiEventJob on nifiEventJob.jobExecution.jobExecutionId = job.jobExecutionId  "
                   + "where nifiEventJob.flowFileId = :flowFileId")
    JpaBatchJobExecution findByFlowFile(@Param("flowFileId") String flowFileId);

    @Query(value = "select job from JpaBatchJobExecution as job "
                   + "join JpaBatchJobInstance jobInstance on jobInstance.id = job.jobInstance.id "
                   + "join JpaOpsManagerFeed feed on feed.id = jobInstance.feed.id "
                   + "left join fetch JpaBatchJobExecutionContextValue executionContext on executionContext.jobExecutionId = job.jobExecutionId "
                   + "where feed.name = :feedName "
                   + "and job.status = 'COMPLETED' "
                   + "and job.endTime > :sinceDate ")
    Set<JpaBatchJobExecution> findJobsForFeedCompletedSince(@Param("feedName") String feedName, @Param("sinceDate") DateTime sinceDate);

    @Query("select job from JpaBatchJobExecution as job "
           + "join JpaBatchJobInstance  jobInstance on jobInstance.jobInstanceId = job.jobInstance.jobInstanceId "
           + "join JpaOpsManagerFeed  feed on feed.id = jobInstance.feed.id "
           + "where feed.name = :feedName "
           + "and job.endTimeMillis = (SELECT max(job2.endTimeMillis)"
           + "     from JpaBatchJobExecution as job2 "
           + "join JpaBatchJobInstance  jobInstance2 on jobInstance2.jobInstanceId = job2.jobInstance.jobInstanceId "
           + "join JpaOpsManagerFeed  feed2 on feed2.id = jobInstance2.feed.id "
           + "where feed2.name = :feedName "
           + "and job2.status = 'COMPLETED')"
           + "order by job.jobExecutionId DESC ")
    List<JpaBatchJobExecution> findLatestCompletedJobForFeed(@Param("feedName") String feedName);


    @Query("select job from JpaBatchJobExecution as job "
           + "join JpaBatchJobInstance  jobInstance on jobInstance.jobInstanceId = job.jobInstance.jobInstanceId "
           + "join JpaOpsManagerFeed  feed on feed.id = jobInstance.feed.id "
           + "where feed.name = :feedName "
           + "and job.endTimeMillis = (SELECT max(job2.endTimeMillis)"
           + "     from JpaBatchJobExecution as job2 "
           + "join JpaBatchJobInstance  jobInstance2 on jobInstance2.jobInstanceId = job2.jobInstance.jobInstanceId "
           + "join JpaOpsManagerFeed  feed2 on feed2.id = jobInstance2.feed.id "
           + "where feed2.name = :feedName )"
           + "order by job.jobExecutionId DESC ")
    List<JpaBatchJobExecution> findLatestFinishedJobForFeed(@Param("feedName") String feedName);

    @Query("select job from JpaBatchJobExecution as job "
           + "join JpaBatchJobInstance  jobInstance on jobInstance.jobInstanceId = job.jobInstance.jobInstanceId "
           + "join JpaOpsManagerFeed  feed on feed.id = jobInstance.feed.id "
           + "where feed.name = :feedName "
           + "and job.startTimeMillis = (SELECT max(job2.startTimeMillis)"
           + "     from JpaBatchJobExecution as job2 "
           + "join JpaBatchJobInstance  jobInstance2 on jobInstance2.jobInstanceId = job2.jobInstance.jobInstanceId "
           + "join JpaOpsManagerFeed  feed2 on feed2.id = jobInstance2.feed.id "
           + "where feed2.name = :feedName )"
           + "order by job.jobExecutionId DESC ")
    List<JpaBatchJobExecution> findLatestJobForFeed(@Param("feedName") String feedName);


    @Query("select job from JpaBatchJobExecution as job "
           + "join JpaBatchJobInstance  jobInstance on jobInstance.jobInstanceId = job.jobInstance.jobInstanceId "
           + "join JpaOpsManagerFeed  feed on feed.id = jobInstance.feed.id "
           + "where feed.name = :feedName "
           + "and job.startTimeMillis = (SELECT max(job2.startTimeMillis)"
           + "     from JpaBatchJobExecution as job2 "
           + "join JpaBatchJobInstance  jobInstance2 on jobInstance2.jobInstanceId = job2.jobInstance.jobInstanceId "
           + "join JpaOpsManagerFeed  feed2 on feed2.id = jobInstance2.feed.id "
           + "where feed2.name = :feedName "
           + "and job2.startTimeMillis >= :startTime)"
           + "order by job.jobExecutionId DESC ")
    List<JpaBatchJobExecution> findLatestJobForFeedWithStartTimeLimit(@Param("feedName") String feedName, @Param("startTime") Long startTime);

    @Query("select job from JpaBatchJobExecution as job "
           + "join JpaBatchJobInstance  jobInstance on jobInstance.jobInstanceId = job.jobInstance.jobInstanceId "
           + "join JpaOpsManagerFeed  feed on feed.id = jobInstance.feed.id "
           + "where feed.name = :feedName "
           + "and job.status in (:jobStatus) ")
    List<JpaBatchJobExecution> findJobsForFeedMatchingStatus(@Param("feedName") String feedName, @Param("jobStatus")BatchJobExecution.JobStatus... jobStatus );


    @Query("select job from JpaBatchJobExecution as job "
           + "join JpaBatchJobInstance  jobInstance on jobInstance.jobInstanceId = job.jobInstance.jobInstanceId "
           + "join JpaOpsManagerFeed  feed on feed.id = jobInstance.feed.id "
           + "where feed.name = :feedName "
           + "and job.startTimeMillis = (SELECT max(job2.startTimeMillis)"
           + "     from JpaBatchJobExecution as job2 "
           + "join JpaBatchJobInstance  jobInstance2 on jobInstance2.jobInstanceId = job2.jobInstance.jobInstanceId "
           + "join JpaOpsManagerFeed  feed2 on feed2.id = jobInstance2.feed.id "
           + "where job2.endTime is null "
           + "and feed2.name = :feedName )"
           + "order by job.jobExecutionId DESC ")
    List<JpaBatchJobExecution> findLatestRunningJobForFeed(@Param("feedName") String feedName);

    @Query(value = "select case when(count(job)) > 0 then true else false end "
                   + " from JpaBatchJobExecution as job "
                   + "join JpaBatchJobInstance  jobInstance on jobInstance.jobInstanceId = job.jobInstance.jobInstanceId "
                   + "join JpaOpsManagerFeed  feed on feed.id = jobInstance.feed.id "
                   + "where feed.name = :feedName "
                   + "and job.endTime is null")
    Boolean isFeedRunning(@Param("feedName") String feedName);
}
