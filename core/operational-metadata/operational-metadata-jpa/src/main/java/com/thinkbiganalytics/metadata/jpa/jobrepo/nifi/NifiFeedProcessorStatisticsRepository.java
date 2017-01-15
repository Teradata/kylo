package com.thinkbiganalytics.metadata.jpa.jobrepo.nifi;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by sr186054 on 9/1/16.
 */
public interface NifiFeedProcessorStatisticsRepository extends JpaRepository<JpaNifiFeedProcessorStats, String>, QueryDslPredicateExecutor<JpaNifiFeedProcessorStats> {

    @Query(value = "select stats from JpaNifiFeedProcessorStats as stats where stats.minEventTime between :startTime and :endTime")
    List<JpaNifiFeedProcessorStats> findWithinTimeWindow(@Param("startTime") DateTime start, @Param("endTime") DateTime end);

    @Query(value = "select max(stats.maxEventId) from JpaNifiFeedProcessorStats as stats")
    Long findMaxEventId();

    @Query(value = "select max(stats.maxEventId) from JpaNifiFeedProcessorStats as stats where stats.clusterNodeId = :clusterNodeId")
    Long findMaxEventId(@Param("clusterNodeId") String clusterNodeId);

}