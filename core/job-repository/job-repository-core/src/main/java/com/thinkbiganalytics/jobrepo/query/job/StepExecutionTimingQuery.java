/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.jobrepo.query.job;


import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.DefaultQueryBuilder;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.model.StepExecutionTiming;
import com.thinkbiganalytics.jobrepo.query.model.TbaJobExecution;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitutionFactory;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by sr186054 on 12/10/15.
 */
public class StepExecutionTimingQuery extends AbstractConstructedQuery<StepExecutionTiming> {

  private Long stepExecutionId;

  public StepExecutionTimingQuery() {
    super();
  }

  public StepExecutionTimingQuery(DatabaseType databaseType) {
    super(databaseType);
  }

  public StepExecutionTimingQuery(DatabaseType databaseType, Long stepExecutionId) {
    super(databaseType);
    this.stepExecutionId = stepExecutionId;
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
        " FROM BATCH_STEP_EXECUTION e WHERE e.STEP_EXECUTION_ID = :executionId ")
        .withNamedParameter("executionId", stepExecutionId).removeDefaultOrderBy();

  }

  @Override
  public RowMapper<StepExecutionTiming> getRowMapper() {
    return new RowMapper() {
      @Override
      public StepExecutionTiming mapRow(ResultSet rs, int i) throws SQLException {
        Long runTime = null;
        Long timeSinceEndTime = null;
        StepExecutionTiming row = new StepExecutionTiming(stepExecutionId) ;
        runTime = rs.getLong("RUN_TIME")*1000;
        timeSinceEndTime = rs.getLong("TIME_SINCE_END_TIME") *1000;
        row.setRunTime(runTime);
        row.setTimeSinceEndTime(timeSinceEndTime);
        return row;
      }
    };
  }
}
