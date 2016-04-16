/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.query.feed;


import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.ColumnFilterQueryModifier;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.model.DefaultFeedHealthQueryResult;
import com.thinkbiganalytics.jobrepo.query.model.FeedHealthQueryResult;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilterUtil;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.support.QueryColumnFilterSqlString;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
/*
SELECT count(e.JOB_INSTANCE_ID) CNT, MAX(e.END_TIME) END_TIME,feed.STRING_VAL FEED_NAME,
'UNHEALTHY' HEALTH
FROM   BATCH_JOB_EXECUTION e
inner join (select job_execution.JOB_INSTANCE_ID, MAX(job_execution.JOB_EXECUTION_ID) JOB_EXECUTION_ID
            FROM BATCH_JOB_EXECUTION job_execution
            INNER JOIN BATCH_JOB_INSTANCE i on job_execution.JOB_INSTANCE_ID = i.JOB_INSTANCE_ID
            GROUP BY job_execution.JOB_INSTANCE_ID) maxe
            on maxe.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID
INNER JOIN BATCH_JOB_EXECUTION_PARAMS feed on feed.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID
     AND feed.KEY_NAME = 'feed'
INNER JOIN BATCH_JOB_EXECUTION_PARAMS jobType on jobType.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID
     AND jobType.KEY_NAME = 'jobType'
     AND jobType.STRING_VAL = 'feed'
     WHERE e.status <> 'ABANDONED' AND (e.status IN('FAILED','UNKNOWN') OR e.EXIT_CODE IN('FAILED'))
GROUP BY feed.STRING_VAL

 */

public class FeedHealthQuery extends AbstractConstructedQuery implements FeedQueryConstants {


  public FeedHealthQuery(DatabaseType databaseType) {
    super(databaseType);
    setDefaultFilter();
  }

  @Override
  public void setColumnFilterList(List list) {
    super.setColumnFilterList(list);
    setDefaultFilter();
  }

  private void setDefaultFilter() {
    ColumnFilter defaultFilter = new QueryColumnFilterSqlString();
    defaultFilter
        .setSqlString(" AND e.status <> 'ABANDONED' AND (e.status IN('FAILED','UNKNOWN') OR e.EXIT_CODE IN('FAILED'))  ");
    getColumnFilterList().add(defaultFilter);
  }

  @Override
  public Query buildQuery() {
    return getQueryBuilder().buildWithFilterQueryModifier(new ColumnFilterQueryModifier() {
      @Override
      public void modifyFilterQueryValue(ColumnFilter columnFilter) {
        String name = columnFilter.getName();
        String strVal = columnFilter.getStringValue();
        columnFilter.setTableAlias("e");
        if ("STRING_VAL".equals(name) || (QUERY_FEED_NAME_COLUMN.equals(name))) {
          columnFilter.setQueryName("STRING_VAL");
          columnFilter.setTableAlias("feed");
        } else {
          ColumnFilterUtil.applyDatabaseTypeDateDiffSql(getDatabaseType(), columnFilter);
        }


      }


    });
  }

  @Override
  public QueryBuilder getQueryBuilder() {

    return newQueryBuilder()
        .select("SELECT count(e.JOB_INSTANCE_ID) CNT, MAX(e.END_TIME) END_TIME,feed.STRING_VAL FEED_NAME, 'UNHEALTHY' HEALTH ")
        .from(
            "BATCH_JOB_EXECUTION e inner join (select job_execution.JOB_INSTANCE_ID, MAX(job_execution.JOB_EXECUTION_ID) JOB_EXECUTION_ID\n"
            +
            "                        FROM BATCH_JOB_EXECUTION job_execution\n" +
            "                        INNER JOIN BATCH_JOB_INSTANCE i on job_execution.JOB_INSTANCE_ID = i.JOB_INSTANCE_ID\n" +
            "                        GROUP BY job_execution.JOB_INSTANCE_ID) maxe\n" +
            "                on maxe.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID\n" +
            "                INNER JOIN BATCH_JOB_EXECUTION_PARAMS feed on feed.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID\n" +
            "                AND feed.KEY_NAME = 'feed'\n" +
            "                INNER JOIN BATCH_JOB_EXECUTION_PARAMS jobType on jobType.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID\n" +
            "                AND jobType.KEY_NAME = 'jobType'\n" +
            "                AND jobType.STRING_VAL = 'feed' ")
        .groupBy(" feed.STRING_VAL ");

  }


  public QueryBuilder getQueryBuilderx() {
    return newQueryBuilder().select(
        "SELECT count(ji.JOB_INSTANCE_ID) CNT, MAX(e.END_TIME) END_TIME,feed.STRING_VAL FEED_NAME, CASE WHEN e.status <> 'ABANDONED' AND (e.status IN('FAILED','UNKNOWN') OR e.EXIT_CODE IN('FAILED')) then 'UNHEALTHY' "
        +
        "  else 'HEALTHY' END as HEALTH")
        .from(
            "BATCH_JOB_EXECUTION e  inner join (select job_execution.JOB_INSTANCE_ID, i.JOB_NAME, MAX(job_execution.JOB_EXECUTION_ID) JOB_EXECUTION_ID "
            +
            "FROM BATCH_JOB_EXECUTION job_execution " +
            "INNER JOIN BATCH_JOB_INSTANCE i on job_execution.JOB_INSTANCE_ID = i.JOB_INSTANCE_ID " +
            "GROUP BY job_execution.JOB_INSTANCE_ID, i.JOB_NAME) maxe on maxe.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID " +
            getDefaultJoins() +
            "AND e.EXIT_CODE != 'NOOP'")
        .groupBy(
            "feed.STRING_VAL, CASE WHEN e.status <> 'ABANDONED' AND (e.status IN('FAILED','UNKNOWN') OR e.EXIT_CODE IN('FAILED')) then 'UNHEALTHY' else 'HEALTHY' END ");

  }

  protected String getDefaultJoins() {
    String query = " INNER JOIN BATCH_JOB_INSTANCE ji on ji.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID ";
    query +=
        " INNER JOIN BATCH_JOB_EXECUTION_PARAMS feed on feed.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID AND feed.KEY_NAME = '"
        + FeedConstants.PARAM__FEED_NAME + "' ";
    query +=
        " INNER JOIN BATCH_JOB_EXECUTION_PARAMS jobType on jobType.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID AND jobType.KEY_NAME = '"
        + FeedConstants.PARAM__JOB_TYPE + "' AND jobType.STRING_VAL = 'feed' ";
    //  query += " INNER JOIN BATCH_JOB_EXECUTION_PARAMS feedParent on feedParent.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID AND feedParent.KEY_NAME = '"+ FeedConstants.PARAM__FEED_IS_PARENT+"' AND feedParent.STRING_VAL = 'true' ";
    return query;
  }

  @Override
  public RowMapper getRowMapper() {
    return new RowMapper() {
      @Override
      public FeedHealthQueryResult mapRow(ResultSet resultSet, int rowNum) throws SQLException {

        Long count = resultSet.getLong("CNT");
        String feed = resultSet.getString("FEED_NAME");
        String health = resultSet.getString("HEALTH");
        Timestamp endTime = resultSet.getTimestamp("END_TIME");
        FeedHealthQueryResult result = new DefaultFeedHealthQueryResult();
        result.setCount(count);
        result.setFeed(feed);
        result.setHealth(health);
        result.setEndTime(endTime);
        return result;


      }
    };

  }
}
