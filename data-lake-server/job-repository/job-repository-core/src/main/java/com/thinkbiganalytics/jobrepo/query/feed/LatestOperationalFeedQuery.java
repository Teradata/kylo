package com.thinkbiganalytics.jobrepo.query.feed;


import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;

import java.util.ArrayList;
import java.util.List;

/**
 * Find the Latest (MAX) JobExecutions for each Feed in the system
 * where the status != STOPPED, STARTED and the EXIT_CODE != STOPPED [NOOP]
 * (NOOP) status is optional via a constructor parameter
 */
public class LatestOperationalFeedQuery extends LatestFeedForStatusQuery {


    public LatestOperationalFeedQuery(DatabaseType databaseType) {
        super(databaseType);
        List<String> notMatchingStatus = new ArrayList<>();
        notMatchingStatus.add("STOPPED");
        notMatchingStatus.add("STARTING");
        List<String> notMatchingExitCodes = new ArrayList<>();
        notMatchingExitCodes.add("STOPPED");
        notMatchingExitCodes.add("NOOP");
        setNotMatchingExitCode(notMatchingExitCodes);
        setNotMatchingJobStatus(notMatchingStatus);
    }


}
