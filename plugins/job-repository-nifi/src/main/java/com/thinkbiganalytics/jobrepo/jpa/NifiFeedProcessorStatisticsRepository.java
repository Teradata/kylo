package com.thinkbiganalytics.jobrepo.jpa;

import com.thinkbiganalytics.jobrepo.jpa.model.NifiFeedProcessorStats;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by sr186054 on 9/1/16.
 */
public interface NifiFeedProcessorStatisticsRepository extends JpaRepository<NifiFeedProcessorStats, String>, QueryDslPredicateExecutor<NifiFeedProcessorStats> {

    @Query(value = "select stats from NifiFeedProcessorStats as stats where stats.minEventTime between :startTime and :endTime")
    List<NifiFeedProcessorStats> findWithinTimeWindow(@Param("startTime") DateTime start, @Param("endTime") DateTime end);


}