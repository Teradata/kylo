package com.thinkbiganalytics.jobrepo.query.feed;


import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.support.FeedQueryUtil;

/**
 * Returns a Count of each Feed, Status in the system
 */
public class LatestFeedNameStatusCountQuery extends FeedNameStatusCountQuery {

    public LatestFeedNameStatusCountQuery(DatabaseType databaseType) {
        super(databaseType);
    }

    public QueryBuilder getQueryBuilder(){

        QueryBuilder q = super.getQueryBuilder();
       q.replaceFrom("BATCH_JOB_EXECUTION e inner join ( " + FeedQueryUtil.latestFeedQuery() +
                        " ) x on x.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID ");
        return q;
    }

}
