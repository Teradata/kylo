package com.thinkbiganalytics.jobrepo.jpa;

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

    private NiFiEventStatisticsRepository statisticsRepository;

    @Autowired
    public NifiEventStatisticsProvider(NiFiEventStatisticsRepository repository) {
        this.statisticsRepository = repository;
    }


    @Override
    public ProvenanceEventSummaryStats create(ProvenanceEventSummaryStats t) {
       return  statisticsRepository.save((NifiEventSummaryStats)t);
    }



    @Override
    public List<? extends ProvenanceEventSummaryStats> findWithinTimeWindow(DateTime start, DateTime end){
        return statisticsRepository.findWithinTimeWindow(start,end);
    }


}
