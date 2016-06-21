package com.thinkbiganalytics.jobrepo.query.job;


import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.rowmapper.JobExecutionRowMapper;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitutionFactory;

import org.springframework.jdbc.core.RowMapper;

/**
 * Find Child JobExecutions for a given Parent JobExecution
 */
public class ChildJobsQuery extends AbstractConstructedQuery {

  private static final String defaultOrderBy = " ORDER BY e.CREATE_TIME desc";
  private String parentJobExecutionId;
  private boolean includeParent;

  public ChildJobsQuery(DatabaseType databaseType, String parentJobExecutionId, boolean includeParent) {
    super(databaseType);
    this.parentJobExecutionId = parentJobExecutionId;
    this.includeParent = includeParent;
  }

  @Override
  public Query buildQuery() {
    return getQueryBuilder().build();
  }

  @Override
  public QueryBuilder getQueryBuilder() {
    return newQueryBuilder()
        .select(
            "SELECT ji.JOB_INSTANCE_ID, ji.JOB_NAME, ji.JOB_KEY, e.JOB_EXECUTION_ID, e.START_TIME, e.END_TIME, "+    DatabaseQuerySubstitutionFactory.JOB_EXECUTION_RUN_TIME_TEMPLATE_STRING + " as RUN_TIME, "
            + DatabaseQuerySubstitutionFactory.getDatabaseSubstitution(getDatabaseType()).getTimeSinceEndTimeSql("e") +" as TIME_SINCE_END_TIME, e.STATUS, e.EXIT_CODE, e.EXIT_MESSAGE, e.CREATE_TIME, e.LAST_UPDATED, e.VERSION, e.JOB_CONFIGURATION_LOCATION,UPPER( jobType.STRING_VAL) as JOB_TYPE ")
        .from("from BATCH_JOB_EXECUTION e " +
              " INNER JOIN BATCH_JOB_INSTANCE ji on ji.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID " +
              "INNER JOIN BATCH_JOB_EXECUTION_PARAMS p on p.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID " +
              " AND p.KEY_NAME = 'parentJobExecutionId' " +
              " AND p.STRING_VAL = :parentJobExecutionId " +
              " LEFT JOIN BATCH_JOB_EXECUTION_PARAMS jobType on jobType.JOB_EXECUTION_ID = e.JOB_INSTANCE_ID AND jobType.KEY_NAME = '"
              + FeedConstants.PARAM__JOB_TYPE + "' ")
        .withNamedParameter("parentJobExecutionId", parentJobExecutionId)
        .defaultOrderBy("CREATE_TIME", "desc");
  }

  @Override
  public RowMapper getRowMapper() {
    return new JobExecutionRowMapper();
  }

  public String getParentJobExecutionId() {
    return parentJobExecutionId;
  }

  public boolean isIncludeParent() {
    return includeParent;
  }
}
