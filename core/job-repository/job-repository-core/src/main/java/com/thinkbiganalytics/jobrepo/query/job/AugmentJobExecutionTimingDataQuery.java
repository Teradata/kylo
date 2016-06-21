/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.jobrepo.query.job;


import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.DefaultQueryBuilder;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.model.TbaJobExecution;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitutionFactory;

import org.springframework.batch.core.JobExecution;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by sr186054 on 12/10/15.
 */
public class AugmentJobExecutionTimingDataQuery extends AbstractConstructedQuery<TbaJobExecution> {

  private JobExecution jobExecution;

  public AugmentJobExecutionTimingDataQuery() {
    super();
  }

  public AugmentJobExecutionTimingDataQuery(DatabaseType databaseType) {
    super(databaseType);
  }

  public AugmentJobExecutionTimingDataQuery(DatabaseType databaseType, JobExecution jobExecution) {
    super(databaseType);
    this.jobExecution = jobExecution;
  }


  @Override
  public Query buildQuery() {
    return getQueryBuilder().build();
  }

  @Override
  public QueryBuilder getQueryBuilder() {
    return DefaultQueryBuilder.newQuery(getDatabaseType()).select(
        "SELECT " + DatabaseQuerySubstitutionFactory.JOB_EXECUTION_RUN_TIME_TEMPLATE_STRING + " as RUN_TIME, "
        + DatabaseQuerySubstitutionFactory.getDatabaseSubstitution(getDatabaseType()).getTimeSinceEndTimeSql("e")
        + " as TIME_SINCE_END_TIME " +
        " FROM BATCH_JOB_EXECUTION e WHERE e.JOB_EXECUTION_ID = :executionId ")
        .withNamedParameter("executionId", jobExecution.getId()).removeDefaultOrderBy();

  }

  @Override
  public RowMapper<TbaJobExecution> getRowMapper() {
    return new RowMapper() {
      @Override
      public TbaJobExecution mapRow(ResultSet rs, int i) throws SQLException {
        Long runTime = null;
        Long timeSinceEndTime = null;
        TbaJobExecution row = new TbaJobExecution(jobExecution);
        runTime = rs.getLong("RUN_TIME")*1000;
        timeSinceEndTime = rs.getLong("TIME_SINCE_END_TIME") *1000;
        row.setRunTime(runTime);
        row.setTimeSinceEndTime(timeSinceEndTime);
        return row;
      }
    };
  }
}
