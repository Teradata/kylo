package com.thinkbiganalytics.metadata.jpa.feed;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.metadata.api.feed.FeedHealth;
import com.thinkbiganalytics.metadata.api.feed.LatestFeedJobExecution;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.JobStatusCount;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.JpaBatchJobExecutionStatusCounts;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.QJpaBatchJobExecution;
import com.thinkbiganalytics.metadata.jpa.support.GenericQueryDslFilter;
import com.thinkbiganalytics.support.FeedNameUtil;

import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sr186054 on 9/15/16.
 */
@Service
public class OpsFeedManagerFeedProvider implements OpsManagerFeedProvider {

    private OpsManagerFeedRepository repository;
    private FeedHealthRepository feedHealthRepository;
    private LatestFeedJobExectionRepository latestFeedJobExectionRepository;

    @Inject
    BatchJobExecutionProvider batchJobExecutionProvider;


    private BatchFeedSummaryCountsRepository batchFeedSummaryCountsRepository;

    @Autowired
    private JPAQueryFactory factory;


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
            return repository.findByFeedIds(ids);
        }
        return null;
    }

    public void save(List<? extends OpsManagerFeed> feeds) {
        repository.save((List<JpaOpsManagerFeed>) feeds);
    }

    @Override
    public OpsManagerFeed save(OpsManagerFeed.ID feedManagerId, String systemName) {
        OpsManagerFeed feed = findById(feedManagerId);
        if (feed == null) {
            feed = new JpaOpsManagerFeed();
            ((JpaOpsManagerFeed) feed).setName(systemName);
            ((JpaOpsManagerFeed) feed).setId((OpsManagerFeedId) feedManagerId);
            repository.save((JpaOpsManagerFeed) feed);
        }
        return feed;
    }

    @Override
    public void delete(OpsManagerFeed.ID id) {
        OpsManagerFeed feed = findById(id);
        if (feed != null) {
            //first delete all jobs for this feed
            deleteFeedJobs(FeedNameUtil.category(feed.getName()),FeedNameUtil.feed(feed.getName()));
            repository.delete(feed.getId());
        }
    }

    public boolean isFeedRunning(OpsManagerFeed.ID id) {
        OpsManagerFeed feed = findById(id);
        if (feed != null) {
            return batchJobExecutionProvider.isFeedRunning(feed.getName());
        }
        return false;
    }

    public List<OpsManagerFeed> findAll(String filter){
        QJpaOpsManagerFeed feed = QJpaOpsManagerFeed.jpaOpsManagerFeed;
        return Lists.newArrayList(repository.findAll(GenericQueryDslFilter.buildFilter(feed, filter)));
    }


    public List<String> getFeedNames(){
        return repository.getFeedNames();
    }

    public List<? extends FeedHealth> getFeedHealth(){
        return feedHealthRepository.findAll();
    }

    private List<? extends FeedHealth> findFeedHealth(String feedName){
        return feedHealthRepository.findByFeedName(feedName);
    }

    public FeedHealth getFeedHealth(String feedName){
        List<? extends FeedHealth> feedHealthList = findFeedHealth(feedName);
        if(feedHealthList != null){
            return feedHealthList.get(0);
        }
        else {
            return null;
        }
    }

    public List<? extends LatestFeedJobExecution> findLatestCheckDataJobs(){
        return latestFeedJobExectionRepository.findCheckDataJobs();
    }

    public List<JobStatusCount> getJobStatusCountByDateFromNow(String feedName,ReadablePeriod period){

        QJpaBatchJobExecution jobExecution = QJpaBatchJobExecution.jpaBatchJobExecution;

        List<BatchJobExecution.JobStatus> runningStatus = ImmutableList.of(BatchJobExecution.JobStatus.STARTED, BatchJobExecution.JobStatus.STARTING);

        com.querydsl.core.types.dsl.StringExpression jobState = new CaseBuilder().when(jobExecution.status.eq(BatchJobExecution.JobStatus.FAILED)).then("FAILED")
            .when(jobExecution.status.in(runningStatus)).then("RUNNING")
            .otherwise(jobExecution.status.stringValue());

        JPAQuery
            query = factory.select(
            Projections.constructor(JpaBatchJobExecutionStatusCounts.class,
                                    jobState.as("status"),
                                    Expressions.constant(feedName),
            jobExecution.startYear,
            jobExecution.startMonth,
            jobExecution.startDay ,
            jobExecution.count().as("count")))
            .from(jobExecution)

            .where(jobExecution.startTime.goe(DateTime.now().minus(period))
            .and(jobExecution.jobInstance.feed.name.eq(feedName)))
        .groupBy(jobState, jobExecution.startYear,
                 jobExecution.startMonth,
                 jobExecution.startDay);

        return (List<JobStatusCount>) query.fetch();

    }

    /**
     * This will call the stored procedure delete_feed_jobs and remove all data, jobs, steps for the feed.
     * @param category
     * @param feed
     */
    public void deleteFeedJobs(String category, String feed){
        repository.deleteFeedJobs(category,feed);
    }


}
