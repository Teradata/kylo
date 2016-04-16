package com.thinkbiganalytics.jobrepo.query.support;


import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;

/**
 * Created by sr186054 on 9/2/15.
 */
public class FeedQueryUtil {

    public static String feedQueryJoin(String jobExecutionTableAlias, String feedNameAlias) {
        return feedQueryJoin(jobExecutionTableAlias, feedNameAlias, true);

    }

    public static String feedQueryJoin(String jobExecutionTableAlias, String feedNameAlias, boolean onlyFeeds) {
        String sql = "";

        if (onlyFeeds) {
            sql += "INNER JOIN BATCH_JOB_EXECUTION_PARAMS p on p.JOB_EXECUTION_ID = " + jobExecutionTableAlias + ".JOB_EXECUTION_ID " +
                    " AND p.KEY_NAME = '" + FeedConstants.PARAM__FEED_IS_PARENT + "'  AND p.STRING_VAL = 'true' ";
        }
        sql += "INNER JOIN BATCH_JOB_EXECUTION_PARAMS jobType on jobType.JOB_EXECUTION_ID = " + jobExecutionTableAlias + ".JOB_EXECUTION_ID " +
                " AND jobType.KEY_NAME = '" + FeedConstants.PARAM__JOB_TYPE + "' ";

        if (onlyFeeds) {
            sql += " AND jobType.STRING_VAL = '" + FeedConstants.PARAM_VALUE__JOB_TYPE_FEED + "' ";
        }
        sql += " INNER JOIN BATCH_JOB_EXECUTION_PARAMS " + feedNameAlias + " on " + feedNameAlias + ".JOB_EXECUTION_ID = " + jobExecutionTableAlias + ".JOB_EXECUTION_ID " +
                " AND " + feedNameAlias + ".KEY_NAME = '" + FeedConstants.PARAM__FEED_NAME + "' ";

        return sql;

    }

    public static String latestFeedQuery() {
        return "  select feedName.STRING_VAL as FEED_NAME, MAX(je.JOB_EXECUTION_ID) JOB_EXECUTION_ID  " +
                "FROM BATCH_JOB_EXECUTION je  " +
                FeedQueryUtil.feedQueryJoin("je", "feedName") +
                "group by feedName.STRING_VAL";
    }
}
