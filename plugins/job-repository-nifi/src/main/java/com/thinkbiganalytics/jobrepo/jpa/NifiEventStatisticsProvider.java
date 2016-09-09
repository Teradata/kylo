package com.thinkbiganalytics.jobrepo.jpa;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.jobrepo.model.ProvenanceEventSummaryStats;
import com.thinkbiganalytics.jobrepo.service.ProvenanceEventSummaryStatsProvider;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by sr186054 on 8/17/16.
 */
@Service
public class NifiEventStatisticsProvider implements ProvenanceEventSummaryStatsProvider {

    @Autowired
    private JPAQueryFactory factory;

    private NifiEventStatisticsRepository statisticsRepository;

    @Autowired
    public NifiEventStatisticsProvider(NifiEventStatisticsRepository repository) {
        this.statisticsRepository = repository;
    }


    @Override
    public ProvenanceEventSummaryStats create(ProvenanceEventSummaryStats t) {
        return statisticsRepository.save((NifiEventSummaryStats) t);
    }

    public List<? extends ProvenanceEventSummaryStats> findForFeedProcessorStatistics(String feedName, TimeFrame timeFrame) {
        DateTime now = DateTime.now();
        return findForFeedProcessorStatistics(feedName, timeFrame.startTimeRelativeTo(now), now);
    }

    public List<? extends ProvenanceEventSummaryStats> findForFeedStatisticsGroupedByTime(String feedName, TimeFrame timeFrame) {
        DateTime now = DateTime.now();
        return findForFeedStatisticsGroupedByTime(feedName, timeFrame.startTimeRelativeTo(now), now);
    }


    @Override
    public List<? extends ProvenanceEventSummaryStats> findWithinTimeWindow(DateTime start, DateTime end){
        return statisticsRepository.findWithinTimeWindow(start, end);
    }


    private Predicate withinDateTime(DateTime start, DateTime end) {
        QNifiEventSummaryStats stats = QNifiEventSummaryStats.nifiEventSummaryStats;
        Predicate p = null;

        if (start == null && end == null) {
            return p;
        }
        if (start != null && end != null) {
            p = stats.minEventTime.goe(start).and(stats.maxEventTime.loe(end));
        } else if (start == null) {
            p = stats.maxEventTime.loe(end);
        } else if (end == null) {
            p = stats.minEventTime.goe(start);
        }
        return p;
    }


    @Override
    public List<? extends ProvenanceEventSummaryStats> findForFeed(String feedName, DateTime start, DateTime end) {
        QNifiEventSummaryStats stats = QNifiEventSummaryStats.nifiEventSummaryStats;
        Iterable<NifiEventSummaryStats> result = statisticsRepository.findAll(stats.feedName.eq(feedName).and(withinDateTime(start, end)));
        if (result != null) {
            return Lists.newArrayList(result);
        }
        return null;
    }


    @Override
    public List<? extends ProvenanceEventSummaryStats> findForFeedProcessorStatistics(String feedName, DateTime start, DateTime end) {
        QNifiEventSummaryStats stats = QNifiEventSummaryStats.nifiEventSummaryStats;
        JPAQuery
            query = factory.select(
            Projections.bean(NifiEventSummaryStats.class,
                             stats.feedName, stats.processorId, stats.processorName,
                             stats.bytesIn.sum().as("bytesIn"), stats.bytesOut.sum().as("bytesOut"), stats.duration.sum().as("duration"),
                             stats.jobsStarted.sum().as("jobsStarted"), stats.jobsFinished.sum().as("jobsFinished"), stats.jobDuration.sum().as("jobDuration"),
                             stats.flowFilesStarted.sum().as("flowFilesStarted"), stats.flowFilesFinished.sum().as("flowFilesFinished"), stats.totalCount.sum().as("totalCount"),
                             stats.maxEventTime.max().as("maxEventTime"), stats.minEventTime.min().as("minEventTime"), stats.jobsFailed.sum().as("jobsFailed"),
                             stats.count().as("resultSetCount"))
        )
            .from(stats)
            .where(stats.feedName.eq(feedName)
                       .and(stats.minEventTime.goe(start)
                                .and(stats.maxEventTime.loe(end))))
            .groupBy(stats.feedName, stats.processorId, stats.processorName)
            .orderBy(stats.processorName.asc());

        return (List<NifiEventSummaryStats>) query.fetch();
    }

    public List<? extends ProvenanceEventSummaryStats> findForFeedStatisticsGroupedByTime(String feedName, DateTime start, DateTime end) {
        QNifiEventSummaryStats stats = QNifiEventSummaryStats.nifiEventSummaryStats;
        JPAQuery
            query = factory.select(
            Projections.bean(NifiEventSummaryStats.class,
                             stats.feedName,
                             stats.bytesIn.sum().as("bytesIn"), stats.bytesOut.sum().as("bytesOut"), stats.duration.sum().as("duration"),
                             stats.jobsStarted.sum().as("jobsStarted"), stats.jobsFinished.sum().as("jobsFinished"), stats.jobDuration.sum().as("jobDuration"),
                             stats.flowFilesStarted.sum().as("flowFilesStarted"), stats.flowFilesFinished.sum().as("flowFilesFinished"),
                             stats.maxEventTime,
                             //stats.maxEventTime,
                             stats.jobsFailed.sum().as("jobsFailed"), stats.totalCount.sum().as("totalCount"),
                             stats.count().as("resultSetCount"))
        )
            .from(stats)
            .where(stats.feedName.eq(feedName)
                       .and(stats.minEventTime.goe(start)
                                .and(stats.maxEventTime.loe(end))))
            .groupBy(stats.feedName, stats.maxEventTime)
            .orderBy(stats.maxEventTime.asc());

        return (List<NifiEventSummaryStats>) query.fetch();
    }
}
