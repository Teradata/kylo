package com.thinkbiganalytics.jobrepo.query.job;

import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.rowmapper.ObjectArrayRowMapper;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;

import org.springframework.jdbc.core.RowMapper;

/**
 * Created by sr186054 on 9/17/15.
 */
public class JobProgressQuery extends AbstractConstructedQuery {


  private String jobExecutionId;

  public void setJobExecutionId(String jobExecutionId) {
    this.jobExecutionId = jobExecutionId;
  }

  public String getJobExecutionId() {
    return jobExecutionId;
  }

  public JobProgressQuery(DatabaseType databaseType) {
    super(databaseType);
  }

  @Override
  public Query buildQuery() {
    return getQueryBuilder().build();
  }

  @Override
  public QueryBuilder getQueryBuilder() {
    QueryBuilder q = newQueryBuilder()
        .select(" SELECT step_names.AVAIL_STEPS, bse.STATUS, bse.JOB_EXECUTION_ID ")
        .from(" FROM " +
              "  BATCH_STEP_EXECUTION   AS bse " +
              "JOIN BATCH_JOB_EXECUTION AS be " +
              "ON (  bse.JOB_EXECUTION_ID  = be.JOB_EXECUTION_ID " +
              "  AND be.JOB_EXECUTION_ID = :executionId " +
              "  ) " +
              "RIGHT OUTER JOIN " +
              "  (  SELECT " +
              "      be.STEP_NAME AS AVAIL_STEPS " +
              "    FROM  BATCH_STEP_EXECUTION   AS be " +
              "    JOIN BATCH_JOB_EXECUTION AS je " +
              "    ON  be.JOB_EXECUTION_ID = je.JOB_EXECUTION_ID " +
              "    JOIN BATCH_JOB_INSTANCE AS ji " +
              "    ON ( je.JOB_INSTANCE_ID = ji.JOB_INSTANCE_ID " +
              "      AND ji.JOB_NAME      = " +
              "        (  SELECT bi.JOB_NAME " +
              "          FROM  BATCH_JOB_INSTANCE     AS bi " +
              "          JOIN BATCH_JOB_EXECUTION AS be " +
              "          ON ( bi.JOB_INSTANCE_ID    = be.JOB_INSTANCE_ID " +
              "            AND be.JOB_EXECUTION_ID = :executionId " +
              "            ) " +
              "        ) " +
              "      ) " +
              "    GROUP BY  ji.JOB_NAME, be.STEP_NAME " +
              "  ) AS step_names ON bse.STEP_NAME = step_names.AVAIL_STEPS")
        .withNamedParameter("executionId", getJobExecutionId());
    return q;
  }


  @Override
  public RowMapper getRowMapper() {
    return new ObjectArrayRowMapper();
  }
}
