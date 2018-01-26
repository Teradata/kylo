package com.thinkbiganalytics.metadata.jpa.feed;

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

import com.google.common.collect.Lists;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertCriteria;
import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.alerts.OperationalAlerts;
import com.thinkbiganalytics.metadata.api.feed.DeleteFeedListener;
import com.thinkbiganalytics.metadata.api.feed.FeedHealth;
import com.thinkbiganalytics.metadata.api.feed.LatestFeedJobExecution;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedChangedListener;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedStatisticsProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedStats;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.JpaBatchJobExecutionStatusCounts;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.QJpaBatchJobExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.QJpaBatchJobInstance;
import com.thinkbiganalytics.metadata.jpa.support.GenericQueryDslFilter;
import com.thinkbiganalytics.metadata.jpa.support.JobStatusDslQueryExpressionBuilder;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

/**
 * Provider allowing access to feeds {@link OpsManagerFeed}
 */
@Service
public class OpsFeedManagerFeedProvider implements OpsManagerFeedProvider {

    private static final Logger log = LoggerFactory.getLogger(OpsFeedManagerFeedProvider.class);
    @Inject
    BatchJobExecutionProvider batchJobExecutionProvider;
    private OpsManagerFeedRepository repository;
    private FeedHealthRepository feedHealthRepository;
    private LatestFeedJobExectionRepository latestFeedJobExectionRepository;
    private BatchFeedSummaryCountsRepository batchFeedSummaryCountsRepository;

    @Autowired
    private JPAQueryFactory factory;

    @Inject
    private AccessController accessController;

    @Inject
    private AlertProvider alertProvider;

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    NifiFeedStatisticsProvider nifiFeedStatisticsProvider;


    /**
     * list of delete feed listeners
     **/
    private List<DeleteFeedListener> deleteFeedListeners = new ArrayList<>();

    private List<OpsManagerFeedChangedListener> feedChangedListeners = new ArrayList<>();


    @Autowired
    public OpsFeedManagerFeedProvider(OpsManagerFeedRepository repository, BatchFeedSummaryCountsRepository batchFeedSummaryCountsRepository,
                                      FeedHealthRepository feedHealthRepository,
                                      LatestFeedJobExectionRepository latestFeedJobExectionRepository) {
        this.repository = repository;
        this.batchFeedSummaryCountsRepository = batchFeedSummaryCountsRepository;
        this.feedHealthRepository = feedHealthRepository;
        this.latestFeedJobExectionRepository = latestFeedJobExectionRepository;
    }

    @Override
    public OpsManagerFeed.ID resolveId(Serializable id) {
        if (id instanceof OpsManagerFeedId) {
            return (OpsManagerFeedId) id;
        } else {
            return new OpsManagerFeedId(id);
        }
    }

    @Override
    public OpsManagerFeed findByName(String name) {
        return repository.findByName(name);
    }


    public OpsManagerFeed findById(OpsManagerFeed.ID id) {
        return repository.findOne(id);
    }

    public List<? extends OpsManagerFeed> findByFeedIds(List<OpsManagerFeed.ID> ids) {
        if (ids != null && !ids.isEmpty()) {
            if (accessController.isEntityAccessControlled()) {
                return repository.findByFeedIdsWithAcl(ids);
            } else {
                return repository.findByFeedIdsWitouthAcl(ids);
            }
        }
        return null;
    }

    public List<? extends OpsManagerFeed> findByFeedNames(Set<String> feedNames) {
        return findByFeedNames(feedNames, true);
    }

    public List<? extends OpsManagerFeed> findByFeedNames(Set<String> feedNames, boolean addAclFilter) {
        if (feedNames != null && !feedNames.isEmpty()) {
            if (addAclFilter) {
                return repository.findByNameWithoutAcl(feedNames);
            } else {
                return repository.findByNameWithoutAcl(feedNames);
            }
        }
        return null;
    }


    public void save(List<? extends OpsManagerFeed> feeds) {
        repository.save((List<JpaOpsManagerFeed>) feeds);
    }

    @Override
    public OpsManagerFeed save(OpsManagerFeed.ID feedManagerId, String systemName, boolean isStream, Long timeBetweenBatchJobs) {
        OpsManagerFeed feed = findById(feedManagerId);
        if (feed == null) {
            feed = new JpaOpsManagerFeed();
            ((JpaOpsManagerFeed) feed).setName(systemName);
            ((JpaOpsManagerFeed) feed).setId((OpsManagerFeedId) feedManagerId);
            ((JpaOpsManagerFeed) feed).setStream(isStream);
            ((JpaOpsManagerFeed) feed).setTimeBetweenBatchJobs(timeBetweenBatchJobs);
        } else {
            ((JpaOpsManagerFeed) feed).setStream(isStream);
            ((JpaOpsManagerFeed) feed).setTimeBetweenBatchJobs(timeBetweenBatchJobs);
        }
        feed = repository.save((JpaOpsManagerFeed) feed);
        notifyOnFeedChanged(feed);
        return feed;
    }

    @Override
    public void delete(OpsManagerFeed.ID id) {
        OpsManagerFeed feed = findById(id);
        if (feed != null) {
            log.info("Deleting feed {} ({})  and all job executions. ", feed.getName(), feed.getId());
            //first delete all jobs for this feed
            deleteFeedJobs(FeedNameUtil.category(feed.getName()), FeedNameUtil.feed(feed.getName()));
            repository.delete(feed.getId());
            //notify the listeners
            notifyOnFeedDeleted(feed);
            log.info("Successfully deleted the feed {} ({})  and all job executions. ", feed.getName(), feed.getId());
        }
    }

    public boolean isFeedRunning(OpsManagerFeed.ID id) {
        OpsManagerFeed feed = findById(id);
        if (feed != null) {
            return batchJobExecutionProvider.isFeedRunning(feed.getName());
        }
        return false;
    }

    public List<OpsManagerFeed> findAll(String filter) {
        QJpaOpsManagerFeed feed = QJpaOpsManagerFeed.jpaOpsManagerFeed;
        return Lists.newArrayList(repository.findAll(GenericQueryDslFilter.buildFilter(feed, filter)));
    }


    public List<String> getFeedNames() {
        if (accessController.isEntityAccessControlled()) {
            return repository.getFeedNamesWithAcl();
        } else {
            return repository.getFeedNamesWithoutAcl();
        }
    }

    public List<? extends FeedHealth> getFeedHealth() {
        return feedHealthRepository.findAll();
    }

    private List<? extends FeedHealth> findFeedHealth(String feedName) {
        if (accessController.isEntityAccessControlled()) {
            return feedHealthRepository.findByFeedNameWithAcl(feedName);
        } else {
            return feedHealthRepository.findByFeedNameWithoutAcl(feedName);
        }
    }

    public FeedHealth getFeedHealth(String feedName) {
        List<? extends FeedHealth> feedHealthList = findFeedHealth(feedName);
        if (feedHealthList != null && !feedHealthList.isEmpty()) {
            return feedHealthList.get(0);
        } else {
            return null;
        }
    }

    public List<? extends LatestFeedJobExecution> findLatestCheckDataJobs() {
        return latestFeedJobExectionRepository.findCheckDataJobs();
    }

    public List<JobStatusCount> getJobStatusCountByDateFromNow(String feedName, ReadablePeriod period) {

        QJpaBatchJobExecution jobExecution = QJpaBatchJobExecution.jpaBatchJobExecution;

        QJpaBatchJobInstance jobInstance = QJpaBatchJobInstance.jpaBatchJobInstance;

        QJpaOpsManagerFeed feed = QJpaOpsManagerFeed.jpaOpsManagerFeed;

        JPAQuery
            query = factory.select(
            Projections.constructor(JpaBatchJobExecutionStatusCounts.class,
                                    JobStatusDslQueryExpressionBuilder.jobState().as("status"),
                                    Expressions.constant(feedName),
                                    jobExecution.startYear,
                                    jobExecution.startMonth,
                                    jobExecution.startDay,
                                    jobExecution.count().as("count")))
            .from(jobExecution)
            .innerJoin(jobInstance).on(jobExecution.jobInstance.jobInstanceId.eq(jobInstance.jobInstanceId))
            .innerJoin(feed).on(jobInstance.feed.id.eq(feed.id))
            .where(jobExecution.startTime.goe(DateTime.now().minus(period))
                       .and(feed.name.eq(feedName))
                       .and(FeedAclIndexQueryAugmentor.generateExistsExpression(feed.id, accessController.isEntityAccessControlled())))
            .groupBy(jobExecution.status,
                     jobExecution.startYear,
                     jobExecution.startMonth,
                     jobExecution.startDay);

        return (List<JobStatusCount>) query.fetch();

    }

    /**
     * This will call the stored procedure delete_feed_jobs and remove all data, jobs, steps for the feed.
     */
    public void deleteFeedJobs(String category, String feed) {
        repository.deleteFeedJobs(category, feed);
    }

    /**
     * This will call the stored procedure abandon_feed_jobs
     */
    public void abandonFeedJobs(String feed) {

        String exitMessage = String.format("Job manually abandoned @ %s", DateTimeUtil.getNowFormattedWithTimeZone());
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        repository.abandonFeedJobs(feed, exitMessage, username);

        //all the alerts manager to handle all job failures
        AlertCriteria criteria = alertProvider.criteria().type(OperationalAlerts.JOB_FALURE_ALERT_TYPE).subtype(feed);
        Iterator<? extends Alert> alerts = alertProvider.getAlerts(criteria);
        StreamSupport.stream(Spliterators.spliteratorUnknownSize(alerts, Spliterator.ORDERED), false)
            .forEach(alert -> alertProvider.respondTo(alert.getId(), (alert1, response) -> response.handle(exitMessage)));
    }

    /**
     * Sets the stream flag for the list of feeds
     *
     * @param feedNames the feed names to update
     * @param isStream  true if stream/ false if not
     */
    public void updateStreamingFlag(Set<String> feedNames, boolean isStream) {
        List<JpaOpsManagerFeed> feeds = (List<JpaOpsManagerFeed>) findByFeedNames(feedNames, false);
        if (feeds != null) {
            for (JpaOpsManagerFeed feed : feeds) {
                //if we move from a stream to a batch we need to stop/complete the running stream job
                if (feed.isStream() && !isStream) {
                    BatchJobExecution jobExecution = batchJobExecutionProvider.findLatestJobForFeed(feed.getName());
                    if (jobExecution != null && !jobExecution.isFinished()) {
                        jobExecution.setStatus(BatchJobExecution.JobStatus.STOPPED);
                        jobExecution.setExitCode(ExecutionConstants.ExitCode.COMPLETED);
                        jobExecution.setEndTime(DateTime.now());
                        batchJobExecutionProvider.save(jobExecution);
                    }
                } else if (!feed.isStream() && isStream) {
                    //if we move from a batch to a stream we need to complete any jobs that are running.
                    batchJobExecutionProvider.findRunningJobsForFeed(feed.getName()).stream().forEach(jobExecution -> {
                        jobExecution.setExitCode(ExecutionConstants.ExitCode.STOPPED);
                        jobExecution.setEndTime(DateTime.now());
                        jobExecution.setExitMessage("Stopping and Abandoning the Job.  The job was running while the feed/template changed from a batch to a stream");
                        jobExecution.setStatus(BatchJobExecution.JobStatus.ABANDONED);
                        batchJobExecutionProvider.save(jobExecution);
                        log.info("Stopping and Abandoning the Job {} for feed {}.  The job was running while the feed/template changed from a batch to a stream", jobExecution.getJobExecutionId(),
                                 feed.getName());
                    });
                }
                feed.setStream(isStream);
            }
            repository.save(feeds);
        }
        feeds.stream().forEach(feed -> notifyOnFeedChanged(feed));

    }


    public List<OpsManagerFeed> findFeedsWithFilter(String filter) {
        QJpaOpsManagerFeed feed = QJpaOpsManagerFeed.jpaOpsManagerFeed;
        BooleanBuilder where = GenericQueryDslFilter.buildFilter(feed, filter);

        JPAQuery
            query = factory.select(feed)
            .from(feed)
            .where(where);
        return query.fetch();
    }

    /**
     * @param feedNames            a set of category.feed names
     * @param timeBetweenBatchJobs a time in millis to suppress new job creation
     */
    @Override
    public void updateTimeBetweenBatchJobs(Set<String> feedNames, Long timeBetweenBatchJobs) {
        List<JpaOpsManagerFeed> feeds = (List<JpaOpsManagerFeed>) findByFeedNames(feedNames, false);
        if (feeds != null) {
            for (JpaOpsManagerFeed feed : feeds) {
                feed.setTimeBetweenBatchJobs(timeBetweenBatchJobs);
            }
            repository.save(feeds);
        }
        feeds.stream().forEach(feed -> notifyOnFeedChanged(feed));
    }

    /**
     * Subscribe to feed deletion events
     *
     * @param listener a delete feed listener
     */
    public void subscribeFeedDeletion(DeleteFeedListener listener) {
        deleteFeedListeners.add(listener);
    }

    public void notifyOnFeedDeleted(OpsManagerFeed feed) {
        deleteFeedListeners.stream().forEach(listener -> listener.onFeedDelete(feed));
    }

    public void subscribe(OpsManagerFeedChangedListener listener) {
        feedChangedListeners.add(listener);
    }

    public void notifyOnFeedChanged(OpsManagerFeed feed) {
        feedChangedListeners.stream().forEach(listener -> listener.onFeedChange(feed));
    }

    @Override
    public DateTime getLastActiveTimeStamp(String feedName) {
        DateTime lastFeedTime = null;
        OpsManagerFeed feed = this.findByName(feedName);
        if (feed.isStream()) {
            NifiFeedStats feedStats = metadataAccess.read(() -> nifiFeedStatisticsProvider.findLatestStatsForFeed(feedName));
            if (feedStats != null && feedStats.getLastActivityTimestamp() != null) {
                lastFeedTime = new DateTime(feedStats.getLastActivityTimestamp());
            }
            else {
                log.warn("feedStats.getLastActivityTimestamp is null for streaming feed {} ",feedName);
            }
        } else {
            BatchJobExecution jobExecution = metadataAccess.read(() -> batchJobExecutionProvider.findLatestCompletedJobForFeed(feedName));
            if (jobExecution != null) {
                lastFeedTime = jobExecution.getEndTime();
            }
        }
        return lastFeedTime;
    }

}
