/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.jobrepo.query.job;


import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.ColumnFilterQueryModifier;
import com.thinkbiganalytics.jobrepo.query.builder.OrderByQueryModifier;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.feed.FeedQueryConstants;
import com.thinkbiganalytics.jobrepo.query.rowmapper.JobExecutionRowMapper;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitutionFactory;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilterUtil;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;

import org.springframework.jdbc.core.RowMapper;

/**
 * Base Query Class for the Jobs Page.
 */
public class LatestJobExecutionsQuery extends AbstractConstructedQuery implements JobQueryConstants {


  public LatestJobExecutionsQuery(DatabaseType databaseType) {
    super(databaseType);
  }


  public QueryBuilder getQueryBuilder() {
    QueryBuilder q = newQueryBuilder()
        .select("SELECT ji.JOB_INSTANCE_ID, maxe.JOB_NAME, ji.JOB_KEY, maxe.JOB_EXECUTION_ID, e.START_TIME, e.END_TIME," +
                DatabaseQuerySubstitutionFactory.JOB_EXECUTION_RUN_TIME_TEMPLATE_STRING + " as RUN_TIME, e.STATUS, "
                + DatabaseQuerySubstitutionFactory.getDatabaseSubstitution(getDatabaseType()).getTimeSinceEndTimeSql("e")
                + " as TIME_SINCE_END_TIME," +
                "e.EXIT_CODE, e.EXIT_MESSAGE, e.CREATE_TIME, e.LAST_UPDATED, e.VERSION, e.JOB_CONFIGURATION_LOCATION, " +
                "UPPER(jobType.STRING_VAL) JOB_TYPE, feed.STRING_VAL FEED_NAME ")

        .from(
            "BATCH_JOB_EXECUTION e  inner join (select job_execution.JOB_INSTANCE_ID, i.JOB_NAME, MAX(job_execution.JOB_EXECUTION_ID) JOB_EXECUTION_ID "
            +
            "FROM BATCH_JOB_EXECUTION job_execution " +
            "INNER JOIN BATCH_JOB_INSTANCE i on job_execution.JOB_INSTANCE_ID = i.JOB_INSTANCE_ID " +
            "GROUP BY job_execution.JOB_INSTANCE_ID, i.JOB_NAME) maxe on maxe.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID " +
            getDefaultJoins())
        .defaultOrderBy("JOB_EXECUTION_ID", " DESC");
    return q;
  }

  /*
  public QueryBuilder getQueryBuilderx() {
      QueryBuilder q = newQueryBuilder()
              .select("SELECT ji.JOB_INSTANCE_ID, ji.JOB_NAME, ji.JOB_KEY, e.JOB_EXECUTION_ID, e.START_TIME, e.END_TIME," +
                      DatabaseQuerySubstitutionFactory.JOB_EXECUTION_RUN_TIME_TEMPLATE_STRING+" as RUN_TIME, e.STATUS, " +
                      "e.EXIT_CODE, e.EXIT_MESSAGE, e.CREATE_TIME, e.LAST_UPDATED, e.VERSION, e.JOB_CONFIGURATION_LOCATION, " +
                      "UPPER(jobType.STRING_VAL) as JOB_TYPE, 'FEED Name ' as FEED_NAME")
              .from(" from BATCH_JOB_EXECUTION e " +
                      getDefaultJoins())
              .defaultOrderBy("JOB_EXECUTION_ID"," DESC");
      return q;
  }

  protected String getDefaultJoinsx() {
      String  query = " INNER JOIN BATCH_JOB_INSTANCE ji on ji.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID ";
      query += " INNER JOIN BATCH_JOB_EXECUTION_PARAMS jobType on jobType.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID AND jobType.KEY_NAME = '"+ FeedRepository.PARAM__JOB_TYPE+"' ";

      return query;
  }
*/
  protected String getDefaultJoins() {
    String query = " INNER JOIN BATCH_JOB_INSTANCE ji on ji.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID ";
    query +=
        " INNER JOIN BATCH_JOB_EXECUTION_PARAMS jobType on jobType.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID AND jobType.KEY_NAME = '"
        + FeedConstants.PARAM__JOB_TYPE + "' ";
    query +=
        " LEFT JOIN BATCH_JOB_EXECUTION_PARAMS feed on feed.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID AND feed.KEY_NAME = '"
        + FeedConstants.PARAM__FEED_NAME + "' ";
    query +=
        " LEFT JOIN BATCH_JOB_EXECUTION_PARAMS feedParent on feedParent.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID AND feedParent.KEY_NAME = '"
        + FeedConstants.PARAM__FEED_IS_PARENT + "' AND feedParent.STRING_VAL = 'true' ";
    return query;
  }

  @Override
  public Query buildQuery() {
    return getQueryBuilder().buildWithQueryModifiers(new ColumnFilterQueryModifier() {
      @Override
      public void modifyFilterQueryValue(ColumnFilter columnFilter) {
        String name = columnFilter.getNameOrFirstFilterName();
        String strVal = columnFilter.getStringValue();

        columnFilter.setTableAlias("e");
        if (jobInstanceColumnNames.contains(name)) {
          columnFilter.setTableAlias("ji");
        } else if (QUERY_JOB_TYPE.equalsIgnoreCase(name)) {
          columnFilter.setTableAlias("jobType");
          columnFilter.setQueryName("STRING_VAL");
        } else if (QUERY_RUN_TIME.equals(name)) {
          columnFilter.setTableAlias("");
          columnFilter.setSqlConditionBeforeOperator(DatabaseQuerySubstitutionFactory.JOB_EXECUTION_RUN_TIME_TEMPLATE_STRING);
        } else if (QUERY_LOOKBACK_TIME.equals(name)) {
          columnFilter.setTableAlias("");
          columnFilter.setSqlString(" AND maxe.START_TIME > NOW() - INTERVAL " + columnFilter.getStringValue() + " MINUTE ");
        } else if (FeedQueryConstants.QUERY_FEED_NAME_COLUMN.equalsIgnoreCase(name)) {
          columnFilter.setTableAlias("feed");
          columnFilter.setQueryName("STRING_VAL");
        } else if ("START_TIME".equalsIgnoreCase(name) || "END_TIME".equalsIgnoreCase(name)) {
          columnFilter.setTableAlias("");
          columnFilter.setSqlString(" AND e." + name + " " + columnFilter.getOperator() + " " + DatabaseQuerySubstitutionFactory
              .getDatabaseSubstitution(getDatabaseType()).toDateSql(strVal) + " ");

        } else {
          ColumnFilterUtil.applyDatabaseTypeDateDiffSql(getDatabaseType(), columnFilter);
        }
        if ("JOB_NAME".equalsIgnoreCase(name)) {
          columnFilter.setTableAlias("maxe");
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
          }

          if ("JOB_NAME".equalsIgnoreCase(column)) {
            orderBy.setTableAlias("maxe");
          } else if ("START_TIME".equalsIgnoreCase(column)) {
            orderBy.setTableAlias("maxe");
            orderBy.setColumnName("JOB_EXECUTION_ID");
          }
        }
      }
    });
  }


  @Override
  public RowMapper getRowMapper() {
    return new JobExecutionRowMapper();
  }
}
