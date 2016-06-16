package com.thinkbiganalytics.jobrepo.query.feed;


import com.thinkbiganalytics.jdbc.util.DatabaseType;

import java.util.ArrayList;
import java.util.List;

/**
 * Find the Latest (MAX) JobExecutions for each Feed in the system where the status != STOPPED, STARTED and the EXIT_CODE !=
 * STOPPED [NOOP] (NOOP) status is optional via a constructor parameter
 */
public class LatestCompletedFeedQuery extends LatestFeedForStatusQuery {


  public LatestCompletedFeedQuery(DatabaseType databaseType) {
    super(databaseType);
    List<String> matchingStatus = new ArrayList<>();
    matchingStatus.add("COMPLETED");
    List<String> matchingExitCodes = new ArrayList<>();
    matchingExitCodes.add("COMPLETED");
    setMatchingExitCode(matchingExitCodes);
    setMatchingJobStatus(matchingStatus);
  }


}
