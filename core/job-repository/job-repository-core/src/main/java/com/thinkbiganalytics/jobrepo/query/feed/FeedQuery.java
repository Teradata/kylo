package com.thinkbiganalytics.jobrepo.query.feed;

import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.ColumnFilterQueryModifier;
import com.thinkbiganalytics.jobrepo.query.builder.OrderByQueryModifier;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.rowmapper.JobExecutionFeedRowMapper;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitutionFactory;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilterUtil;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.support.FeedQueryUtil;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;

import org.springframework.jdbc.core.RowMapper;

/**
 * Main Query for the Feeds Section of the Pipeline Controller
 */
public class FeedQuery extends AbstractConstructedQuery implements FeedQueryConstants {


  public FeedQuery(DatabaseType databaseType) {
    super(databaseType);
  }

  public QueryBuilder getQueryBuilder() {
    return newQueryBuilder()
        .select("SELECT ji.JOB_INSTANCE_ID, ji.JOB_NAME, ji.JOB_KEY, e.JOB_EXECUTION_ID, " +
                "e.START_TIME, COALESCE(childJobs.END_TIME,e.END_TIME) as END_TIME, " +
                DatabaseQuerySubstitutionFactory.FEED_EXECUTION_RUN_TIME_TEMPLATE_STRING + " as RUN_TIME, " +
                "COALESCE(childJobs.STATUS,e.STATUS) as STATUS, COALESCE(childJobs.EXIT_CODE,e.EXIT_CODE) as EXIT_CODE, " +
                "e.EXIT_MESSAGE, e.CREATE_TIME, e.LAST_UPDATED, e.VERSION, e.JOB_CONFIGURATION_LOCATION, feed.STRING_VAL as FEED_NAME, "
                +
                "'FEED' as JOB_TYPE")
        .from("BATCH_JOB_EXECUTION e " +
              FeedQueryUtil.feedQueryJoin("e", "feed")
              + " INNER JOIN BATCH_JOB_INSTANCE ji on ji.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID " +
              " LEFT JOIN ( " +
              " SELECT MAX(e.END_TIME) as END_TIME ,e.STATUS, e.EXIT_CODE, p.STRING_VAL as PARENT_JOB_EXECUTION_ID" +
              " FROM BATCH_JOB_EXECUTION e " +
              " INNER JOIN BATCH_JOB_INSTANCE ji on ji.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID " +
              " INNER JOIN BATCH_JOB_EXECUTION_PARAMS p on p.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID " +
              " AND p.KEY_NAME = 'parentJobExecutionId' " +
              " GROUP BY e.STATUS, e.EXIT_CODE, p.STRING_VAL ) childJobs on childJobs.PARENT_JOB_EXECUTION_ID = e.JOB_EXECUTION_ID ")
        .defaultOrderBy("JOB_EXECUTION_ID", "DESC");

  }


  public Query buildQuery() {
    return getQueryBuilder()
        .buildWithQueryModifiers(new ColumnFilterQueryModifier() {
          @Override
          public void modifyFilterQueryValue(ColumnFilter columnFilter) {
            String name = columnFilter.getName();
            String strVal = columnFilter.getStringValue();
            if (QUERY_LATEST_FEED.equals(name)) {
              if (strVal.equalsIgnoreCase("true")) {
                columnFilter.setSqlString(" AND latestFeeds.JOB_EXECUTION_ID IS NOT NULL ");
              } else if (strVal.equalsIgnoreCase("false")) {
                columnFilter.setSqlString(" AND latestFeeds.JOB_EXECUTION_ID IS NULL ");
              }
            } else {
              columnFilter.setTableAlias("e");
              if (QUERY_RUN_TIME.equals(name)) {
                columnFilter.setTableAlias("");
                columnFilter
                    .setSqlConditionBeforeOperator(DatabaseQuerySubstitutionFactory.FEED_EXECUTION_RUN_TIME_TEMPLATE_STRING);
              }

              if (jobInstanceColumnNames.contains(name)) {
                columnFilter.setTableAlias("ji");
              }
              if (("STRING_VAL".equals(name) || (QUERY_FEED_NAME_COLUMN.equals(name)) && !QUERY_ALL_VALUE
                  .equalsIgnoreCase(strVal))) {
                columnFilter.setQueryName("STRING_VAL");
                columnFilter.setTableAlias("feed");
              } else {
                ColumnFilterUtil.applyDatabaseTypeDateDiffSql(getDatabaseType(), columnFilter);
              }


            }
          }
        }, new OrderByQueryModifier() {
          @Override
          public void modifyOrderByQueryName(OrderBy orderBy) {
            if (orderBy != null) {
              String column = orderBy.getColumnName();
              String dir = orderBy.getDir();
              orderBy.setTableAlias("e");
              if (QUERY_FEED_NAME_COLUMN.equalsIgnoreCase(column)) {
                orderBy.setQueryName("STRING_VAL");
                orderBy.setTableAlias("feed");
              } else if (QUERY_RUN_TIME.equals(column)) {
                orderBy.setTableAlias("");
                orderBy.setQueryName(DatabaseQuerySubstitutionFactory.FEED_EXECUTION_RUN_TIME_TEMPLATE_STRING);
              } else if (jobInstanceColumnNames.contains(column)) {
                orderBy.setTableAlias("ji");
              } else if (QUERY_LATEST_FEED.equals(column)) {
                orderBy.setQueryName("JOB_EXECUTION_ID");
                orderBy.setTableAlias("latestFeeds");
              }
            }
          }
        });
  }

  @Override
  public RowMapper getRowMapper() {
    return new JobExecutionFeedRowMapper();
  }


}
