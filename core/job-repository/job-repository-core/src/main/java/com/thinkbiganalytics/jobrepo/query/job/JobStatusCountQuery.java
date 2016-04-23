package com.thinkbiganalytics.jobrepo.query.job;


import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCountResult;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by sr186054 on 9/2/15.
 */
public class JobStatusCountQuery extends AbstractConstructedQuery<JobStatusCount> {

  public JobStatusCountQuery(DatabaseType databaseType) {
    super(databaseType);
  }

  @Override
  public Query buildQuery() {
    return getQueryBuilder().build();
  }

  @Override
  public QueryBuilder getQueryBuilder() {
    QueryBuilder q = newQueryBuilder()
        .select("select count(*) CNT, e.STATUS ")
        .from("FROM  BATCH_JOB_EXECUTION e ")
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

  @Override
  public RowMapper<JobStatusCount> getRowMapper() {
    return new RowMapper<JobStatusCount>() {
      @Override
      public JobStatusCount mapRow(ResultSet resultSet, int i) throws SQLException {

        JobStatusCount jobStatusCount = new JobStatusCountResult();
        jobStatusCount.setCount(resultSet.getLong("CNT"));
        jobStatusCount.setStatus(resultSet.getString("STATUS"));
        return jobStatusCount;
      }
    };
  }
}
