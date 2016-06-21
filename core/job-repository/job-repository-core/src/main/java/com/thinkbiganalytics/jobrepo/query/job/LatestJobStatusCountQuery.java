package com.thinkbiganalytics.jobrepo.query.job;


import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;

/**
 * Created by sr186054 on 9/2/15.
 */
public class LatestJobStatusCountQuery extends JobStatusCountQuery {

  public LatestJobStatusCountQuery(DatabaseType databaseType) {
    super(databaseType);
  }

  @Override
  public QueryBuilder getQueryBuilder() {
    QueryBuilder q = newQueryBuilder()
        .select("select count(*) CNT, e.STATUS ")
        .from("FROM  BATCH_JOB_EXECUTION e " +
              "inner join ( " + latestJobsQuery() +
              " ) x on x.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID ")
        .groupBy("group by STATUS");
    return q;
  }


  private String latestJobsQuery() {
    return "select MAX(je.JOB_EXECUTION_ID) JOB_EXECUTION_ID  " +
           " FROM BATCH_JOB_EXECUTION je  " +
           " inner join BATCH_JOB_INSTANCE ji on ji.JOB_INSTANCE_ID = je.JOB_INSTANCE_ID " +
           " WHERE STATUS != 'STARTING' " +
           " group by ji.JOB_NAME ";
  }

}
