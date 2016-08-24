package com.thinkbiganalytics.jobrepo.service;

import com.thinkbiganalytics.jobrepo.model.ProvenanceEventSummaryStats;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface ProvenanceEventSummaryStatsProvider {

    ProvenanceEventSummaryStats create(ProvenanceEventSummaryStats t);

    List<? extends ProvenanceEventSummaryStats> findWithinTimeWindow(DateTime start, DateTime end);
}
