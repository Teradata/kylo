package com.thinkbiganalytics.jobrepo.jpa;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface NifiEventStatisticsRepository extends JpaRepository<NifiEventSummaryStats, String>, QueryDslPredicateExecutor<NifiEventSummaryStats> {

    @Query(value = "select stats from NifiEventSummaryStats as stats where stats.minEventTime between :startTime and :endTime")
    List<NifiEventSummaryStats> findWithinTimeWindow(@Param("startTime")DateTime start, @Param("endTime") DateTime end);


}
