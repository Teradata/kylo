package com.thinkbiganalytics.jobrepo.query.feed;


import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Find the Latest (MAX) JobExecutions for each Feed in the system
 */
public class LatestFeedForStatusQuery extends FeedQuery {

    private List<String> matchingExitCode;
    private List<String> matchingJobStatus;
    private List<String> notMatchingExitCode;
    private List<String> notMatchingJobStatus;

    public LatestFeedForStatusQuery(DatabaseType databaseType) {
        super(databaseType);
    }


    private String listToQueryString(List<String> list) {
        if (list != null && !list.isEmpty()) {
            String str = StringUtils.join(list, "','");
            str = "'" + str + "'";
            return str;
        }
        return null;
    }

    private String getNotMatchingJobStatusString() {
        return getQueryString(this.notMatchingJobStatus, " a.STATUS ", "NOT IN");
    }

    private String getMatchingJobStatusString() {
        return getQueryString(this.matchingJobStatus, " a.STATUS ", "IN");
    }

    private String getNotMatchingExitCodesString() {
        return getQueryString(this.notMatchingExitCode, " a.EXIT_CODE ", "NOT IN");
    }

    private String getMatchingExitCodesString() {
        return getQueryString(this.matchingExitCode, " a.EXIT_CODE ", "IN");
    }

    private String getQueryString(List<String> list, String column, String inClause) {
        if (list != null && !list.isEmpty()) {
            return " AND " + column + " " + inClause + "(" + listToQueryString(list) + ") ";
        }
        return "";
    }

    private String getFilters() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMatchingJobStatusString())
                .append(getNotMatchingJobStatusString())
                .append(getMatchingExitCodesString())
                .append(getNotMatchingExitCodesString());
        return sb.toString();
    }


    private String getFrom() {
        String from = "FROM BATCH_JOB_EXECUTION e  " +
                "INNER JOIN ( SELECT b.STRING_VAL , MAX(a.JOB_EXECUTION_ID) AS JOB_EXECUTION_ID  " +
                "                       FROM BATCH_JOB_EXECUTION a" +
                "                       INNER JOIN BATCH_JOB_EXECUTION_PARAMS b  " +
                "                       on a.JOB_EXECUTION_ID = b.JOB_EXECUTION_ID  " +
                "                       AND b.KEY_NAME = 'feed'  ";
        from += getFilters();
        from +=
                "                       INNER JOIN BATCH_JOB_EXECUTION_PARAMS c on  c.KEY_NAME='jobType' AND c.STRING_VAL = 'FEED'" +
                        "                       and c.JOB_EXECUTION_ID = b.JOB_EXECUTION_ID" +
                        "                       GROUP BY b.STRING_VAL ) feed on feed.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID " +
                        "inner join BATCH_JOB_INSTANCE ji on ji.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID " +
                        "LEFT JOIN (  " +
                        "                 SELECT MAX(e.END_TIME) as END_TIME ,e.STATUS, e.EXIT_CODE, p.STRING_VAL as PARENT_JOB_EXECUTION_ID " +
                        "                 FROM BATCH_JOB_EXECUTION e  " +
                        "                 INNER JOIN BATCH_JOB_INSTANCE ji on ji.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID  " +
                        "                 INNER JOIN BATCH_JOB_EXECUTION_PARAMS p on p.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID  " +
                        "                 AND p.KEY_NAME = 'parentJobExecutionId'  " +
                        "                 GROUP BY e.STATUS, e.EXIT_CODE, p.STRING_VAL ) childJobs on childJobs.PARENT_JOB_EXECUTION_ID = e.JOB_EXECUTION_ID ";
        return from;
    }

    @Override
    public QueryBuilder getQueryBuilder() {
        QueryBuilder feedQueryBuilder = super.getQueryBuilder();
        feedQueryBuilder.replaceFrom(getFrom());

        return feedQueryBuilder;
    }

    public void setMatchingExitCode(List<String> matchingExitCode) {
        this.matchingExitCode = matchingExitCode;
    }

    public void setMatchingJobStatus(List<String> matchingJobStatus) {
        this.matchingJobStatus = matchingJobStatus;
    }

    public void setNotMatchingExitCode(List<String> notMatchingExitCode) {
        this.notMatchingExitCode = notMatchingExitCode;
    }

    public void setNotMatchingJobStatus(List<String> notMatchingJobStatus) {
        this.notMatchingJobStatus = notMatchingJobStatus;
    }
}
