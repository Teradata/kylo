package com.thinkbiganalytics.jobrepo.query.job;

import com.thinkbiganalytics.jobrepo.common.constants.CheckDataStepConstants;
import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.ColumnFilterQueryModifier;
import com.thinkbiganalytics.jobrepo.query.builder.OrderByQueryModifier;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.rowmapper.CheckDataJobRowMapper;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitutionFactory;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;

import org.springframework.jdbc.core.RowMapper;

/**
 * Base Query Class for the Jobs -> Check Data Jobs Tab. Shows Job Information as well as the Validation Key and Validation
 * Message supplied from the Check Data Jobs
 */
public class CheckDataAllJobsQuery extends AbstractConstructedQuery implements JobQueryConstants {

  public static final String QUERY_IS_VALID = "IS_VALID";
  public static final String QUERY_VALIDATION_MESSAGE = "VALIDATION_MESSAGE";
  public static final String QUERY_FEED_NAME = "FEED_NAME";

  public CheckDataAllJobsQuery(DatabaseType databaseType) {
    super(databaseType);
  }

  @Override
  public QueryBuilder getQueryBuilder() {
    QueryBuilder q = newQueryBuilder()
        .select("SELECT ji.JOB_INSTANCE_ID, ji.JOB_NAME, ji.JOB_KEY, e.JOB_EXECUTION_ID, e.START_TIME, e.END_TIME as END_TIME, " +
                DatabaseQuerySubstitutionFactory.JOB_EXECUTION_RUN_TIME_TEMPLATE_STRING + " as RUN_TIME, e.STATUS as STATUS,"
                + DatabaseQuerySubstitutionFactory.getDatabaseSubstitution(getDatabaseType()).getTimeSinceEndTimeSql("e") +" as TIME_SINCE_END_TIME, "+
                "e.EXIT_CODE as EXIT_CODE, e.EXIT_MESSAGE, e.CREATE_TIME, e.LAST_UPDATED, e.VERSION, e.JOB_CONFIGURATION_LOCATION, "
                +
                "p2.STRING_VAL as FEED_NAME, '" + FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK + "' as JOB_TYPE, " +
                "exec_ctx1.STRING_VAL as " + QUERY_IS_VALID + ", exec_ctx2.STRING_VAL as " + QUERY_VALIDATION_MESSAGE + " ")
        .from(" from  BATCH_JOB_EXECUTION e"
              + " INNER JOIN BATCH_JOB_EXECUTION_PARAMS jobType on jobType.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID " +
              " and jobType.KEY_NAME = :jobType " +
              " and UPPER(jobType.STRING_VAL) = :check " +
              " LEFT JOIN BATCH_JOB_EXECUTION_PARAMS p2 on p2.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID " +
              " and p2.KEY_NAME = :feed " +
              " LEFT JOIN BATCH_JOB_INSTANCE ji on ji.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID " +
              " LEFT JOIN BATCH_EXECUTION_CONTEXT_VALUES exec_ctx1 ON exec_ctx1.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID " +
              " AND exec_ctx1.KEY_NAME = :validation_key " +
              " LEFT JOIN BATCH_EXECUTION_CONTEXT_VALUES exec_ctx2 ON exec_ctx2.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID " +
              " AND exec_ctx2.KEY_NAME = :validation_message_key ")
        .withNamedParameter("jobType", FeedConstants.PARAM__JOB_TYPE)
        .withNamedParameter("check", FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK)
        .withNamedParameter("feed", FeedConstants.PARAM__FEED_NAME)
        .withNamedParameter("validation_key", CheckDataStepConstants.VALIDATION_KEY)
        .withNamedParameter("validation_message_key", CheckDataStepConstants.VALIDATION_MESSAGE_KEY);
    return q;
  }

  @Override
  public Query buildQuery() {
    return getQueryBuilder().buildWithQueryModifiers(new ColumnFilterQueryModifier() {
      @Override
      public void modifyFilterQueryValue(ColumnFilter columnFilter) {
        String name = columnFilter.getNameOrFirstFilterName();
        String strVal = columnFilter.getStringValue();

        if (QUERY_LATEST_JOB.equals(name)) {
          if (strVal.equalsIgnoreCase("true")) {
            columnFilter.setSqlString(" AND latestJobs.JOB_EXECUTION_ID IS NOT NULL ");
          } else if (strVal.equalsIgnoreCase("false")) {
            columnFilter.setSqlString(" AND latestJobs.JOB_EXECUTION_ID IS NULL ");
          }
        } else {
          columnFilter.setTableAlias("e");
          if (jobInstanceColumnNames.contains(name)) {
            columnFilter.setTableAlias("ji");
          } else if (QUERY_JOB_TYPE.equalsIgnoreCase(name)) {
            //No need to apply the JobType.. it is always CHECK
            columnFilter.setApplyToWhereClause(false);
          } else if (QUERY_RUN_TIME.equals(name)) {
            columnFilter.setTableAlias("");
            columnFilter.setSqlConditionBeforeOperator(DatabaseQuerySubstitutionFactory.JOB_EXECUTION_RUN_TIME_TEMPLATE_STRING);
          } else if (QUERY_LOOKBACK_TIME.equals(name)) {
            columnFilter.setTableAlias("");
            columnFilter.setSqlString(" AND e.START_TIME > NOW() - INTERVAL " + columnFilter.getStringValue() + " MINUTE ");
          } else if (QUERY_FEED_NAME.equals(name)) {
            columnFilter.setTableAlias("p2");
            columnFilter.setQueryName("STRING_VAL");
          }
          if (QUERY_IS_VALID.equals(name)) {
            columnFilter.setTableAlias("exec_ctx1");
            columnFilter.setQueryName("STRING_VAL");
          }
          if (QUERY_VALIDATION_MESSAGE.equals(name)) {
            columnFilter.setTableAlias("exec_ctx2");
            columnFilter.setQueryName("STRING_VAL");
          }
        }
      }
    }, new OrderByQueryModifier() {
      @Override
      public void modifyOrderByQueryName(OrderBy orderBy) {
        if (orderBy != null) {
          String column = orderBy.getColumnName();
          orderBy.setTableAlias("e");
          if (jobInstanceColumnNames.contains(column)) {
            orderBy.setTableAlias("ji");
          } else if (QUERY_RUN_TIME.equals(column)) {
            orderBy.setTableAlias("");
            orderBy.setQueryName(DatabaseQuerySubstitutionFactory.JOB_EXECUTION_RUN_TIME_TEMPLATE_STRING);
          } else if (QUERY_JOB_TYPE.equalsIgnoreCase(column)) {
            orderBy.setTableAlias("jobType");
            orderBy.setQueryName("STRING_VAL");
          } else if (QUERY_LATEST_JOB.equals(column)) {
            orderBy.setTableAlias("latestJobs");
            orderBy.setQueryName("JOB_EXECUTION_ID");
          } else if (QUERY_IS_VALID.equalsIgnoreCase(column)) {
            orderBy.setQueryName("STRING_VAL");
            orderBy.setTableAlias("exec_ctx1");
          } else if (QUERY_VALIDATION_MESSAGE.equals(column)) {
            orderBy.setTableAlias("exec_ctx2");
            orderBy.setQueryName("STRING_VAL");
          }

        }
      }
    });
  }

  @Override
  public RowMapper getRowMapper() {
    return new CheckDataJobRowMapper();
  }


}
