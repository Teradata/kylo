package com.thinkbiganalytics.metadata.jpa.jobrepo.nifi;

import com.google.common.collect.Lists;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStats;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by sr186054 on 8/17/16.
 */
@Service
public class NifiFeedProcessorStatisticsProvider implements com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStatisticsProvider {

    @Autowired
    private JPAQueryFactory factory;

    private NifiFeedProcessorStatisticsRepository statisticsRepository;

    private NifiEventRepository nifiEventRepository;

    @Autowired
    public NifiFeedProcessorStatisticsProvider(NifiFeedProcessorStatisticsRepository repository, NifiEventRepository nifiEventRepository) {
        this.statisticsRepository = repository;
        this.nifiEventRepository = nifiEventRepository;
    }


    @Override
    public NifiFeedProcessorStats create(NifiFeedProcessorStats t) {
        return statisticsRepository.save((JpaNifiFeedProcessorStats) t);
    }

    public List<? extends JpaNifiFeedProcessorStats> findForFeedProcessorStatistics(String feedName, TimeFrame timeFrame) {
        DateTime now = DateTime.now();
        return findForFeedProcessorStatistics(feedName, timeFrame.startTimeRelativeTo(now), now);
    }

    public List<? extends JpaNifiFeedProcessorStats> findForFeedStatisticsGroupedByTime(String feedName, TimeFrame timeFrame) {
        DateTime now = DateTime.now();
        return findForFeedStatisticsGroupedByTime(feedName, timeFrame.startTimeRelativeTo(now), now);
    }


    @Override
    public List<? extends JpaNifiFeedProcessorStats> findWithinTimeWindow(DateTime start, DateTime end) {
        return statisticsRepository.findWithinTimeWindow(start, end);
    }


    private Predicate withinDateTime(DateTime start, DateTime end) {
        QJpaNifiFeedProcessorStats stats = QJpaNifiFeedProcessorStats.jpaNifiFeedProcessorStats;
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
    public List<? extends JpaNifiFeedProcessorStats> findForFeed(String feedName, DateTime start, DateTime end) {
        QJpaNifiFeedProcessorStats stats = QJpaNifiFeedProcessorStats.jpaNifiFeedProcessorStats;
        Iterable<JpaNifiFeedProcessorStats> result = statisticsRepository.findAll(stats.feedName.eq(feedName).and(withinDateTime(start, end)));
        if (result != null) {
            return Lists.newArrayList(result);
        }
        return null;
    }


    @Override
    public List<? extends JpaNifiFeedProcessorStats> findForFeedProcessorStatistics(String feedName, DateTime start, DateTime end) {
        QJpaNifiFeedProcessorStats stats = QJpaNifiFeedProcessorStats.jpaNifiFeedProcessorStats;
        JPAQuery
            query = factory.select(
            Projections.bean(JpaNifiFeedProcessorStats.class,
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

        return (List<JpaNifiFeedProcessorStats>) query.fetch();
    }

    public List<? extends JpaNifiFeedProcessorStats> findForFeedStatisticsGroupedByTime(String feedName, DateTime start, DateTime end) {
        QJpaNifiFeedProcessorStats stats = QJpaNifiFeedProcessorStats.jpaNifiFeedProcessorStats;
        JPAQuery
            query = factory.select(
            Projections.bean(JpaNifiFeedProcessorStats.class,
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

        return (List<JpaNifiFeedProcessorStats>) query.fetch();
    }


    @Override
    public Long findMaxEventId(String clusterNodeId) {
        Long eventId = -1L;
        if (StringUtils.isNotBlank(clusterNodeId)) {
            eventId = statisticsRepository.findMaxEventId(clusterNodeId);
            if (eventId == null) {
                eventId = nifiEventRepository.findMaxEventId(clusterNodeId);
            }
        } else {
            eventId = findMaxEventId();
        }
        return eventId;
    }

    public Long findMaxEventId() {
        Long eventId = statisticsRepository.findMaxEventId();
        if (eventId == null) {
            eventId = nifiEventRepository.findMaxEventId();
        }
        return eventId;
    }
}
