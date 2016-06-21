/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.jobrepo.query.job;


import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.DefaultQueryBuilder;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.model.JobStepQueryRow;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by sr186054 on 12/10/15.
 */
public class JobStepQuery extends AbstractConstructedQuery {

  private Long jobExecutionId;

  public JobStepQuery() {
    super();
  }

  public JobStepQuery(DatabaseType databaseType) {
    super(databaseType);
  }

  public JobStepQuery(DatabaseType databaseType, Long jobExecutionId) {
    super(databaseType);
    this.jobExecutionId = jobExecutionId;
  }

  public Long getJobExecutionId() {
    return jobExecutionId;
  }

  public void setJobExecutionId(Long jobExecutionId) {
    this.jobExecutionId = jobExecutionId;
  }

  @Override
  public Query buildQuery() {
    return getQueryBuilder().build();
  }

  @Override
  public QueryBuilder getQueryBuilder() {
    return DefaultQueryBuilder.newQuery(getDatabaseType()).select(
        "SELECT MAX(se.JOB_EXECUTION_ID) as \"JOB_EXECUTION_ID\", se.STEP_NAME,MAX(se.STEP_EXECUTION_ID) as \"STEP_EXECUTION_ID\", je.JOB_INSTANCE_ID")
        .from("FROM BATCH_STEP_EXECUTION se\n" +
              "INNER JOIN BATCH_JOB_EXECUTION je on je.JOB_EXECUTION_ID = se.JOB_EXECUTION_ID\n" +
              "where je.JOB_INSTANCE_ID = (SELECT JOB_INSTANCE_ID from BATCH_JOB_EXECUTION WHERE JOB_EXECUTION_ID = :executionId )\n"
              +
              " AND je.JOB_EXECUTION_ID <= :executionId " +
              "GROUP BY  se.STEP_NAME,je.JOB_INSTANCE_ID ")
        .withNamedParameter("executionId", jobExecutionId)
        .defaultOrderBy("MAX(se.STEP_EXECUTION_ID)", "asc");

  }

  @Override
  public RowMapper getRowMapper() {
    return new RowMapper() {
      @Override
      public Object mapRow(ResultSet resultSet, int i) throws SQLException {
        JobStepQueryRow row = new JobStepQueryRow();
        row.setJobExecutionId(resultSet.getLong("JOB_EXECUTION_ID"));
        row.setJobInstanceId(resultSet.getLong("JOB_INSTANCE_ID"));
        row.setStepExecutionId(resultSet.getLong("STEP_EXECUTION_ID"));
        row.setStepName(resultSet.getString("STEP_NAME"));
        return row;
      }
    };
  }
}
