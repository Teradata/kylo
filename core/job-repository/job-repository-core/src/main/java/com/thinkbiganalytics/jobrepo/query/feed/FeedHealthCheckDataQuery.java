/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.query.feed;


import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.ColumnFilterQueryModifier;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.model.DefaultFeedHealthQueryResult;
import com.thinkbiganalytics.jobrepo.query.model.FeedHealthQueryResult;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilterUtil;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;


public class FeedHealthCheckDataQuery extends AbstractConstructedQuery implements FeedQueryConstants {


  public FeedHealthCheckDataQuery(DatabaseType databaseType) {
    super(databaseType);
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
          columnFilter.setQueryName("FEED_NAME");
          columnFilter.setTableAlias("maxe");
        } else {
          ColumnFilterUtil.applyDatabaseTypeDateDiffSql(getDatabaseType(), columnFilter);
        }


      }


    });
  }

  @Override
  public QueryBuilder getQueryBuilder() {
    return newQueryBuilder().select(
        " SELECT CASE WHEN e.status <> 'ABANDONED' AND (e.status IN('FAILED','UNKNOWN') OR e.EXIT_CODE IN('FAILED')) then\n" +
        "    1 else 0 end CNT, MAX(e.END_TIME) END_TIME,\n" +
        "maxe.FEED_NAME FEED_NAME, \n" +
        " 'UNHEALTHY' as HEALTH        \n" +
        " FROM   BATCH_JOB_EXECUTION e  \n" +
        "  inner join (select MAX(job_execution.JOB_EXECUTION_ID) JOB_EXECUTION_ID , feed.STRING_VAL as FEED_NAME\n" +
        "               FROM BATCH_JOB_EXECUTION job_execution \n" +
        "               INNER JOIN BATCH_JOB_INSTANCE i on job_execution.JOB_INSTANCE_ID = i.JOB_INSTANCE_ID \n" +
        "               INNER JOIN BATCH_JOB_EXECUTION_PARAMS jobType on jobType.JOB_EXECUTION_ID = job_execution.JOB_EXECUTION_ID \n"
        +
        " AND jobType.KEY_NAME = 'jobType'  and jobType.STRING_VAL in ('CHECK','check')   \n" +
        "                 INNER JOIN BATCH_JOB_EXECUTION_PARAMS feed on feed.JOB_EXECUTION_ID = job_execution.JOB_EXECUTION_ID AND feed.KEY_NAME = 'feed' \n"
        +
        "                where job_execution.STATUS not in('STARTING','STARTED')\n" +
        "                group by feed.string_val) maxe on maxe.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID "
    ).groupBy(
        " GROUP BY  CASE WHEN e.status <> 'ABANDONED' AND (e.status IN('FAILED','UNKNOWN') OR e.EXIT_CODE IN('FAILED')) then 1 else 0 end, maxe.FEED_NAME ");
  }


  protected String getDefaultJoins() {
    String query = " INNER JOIN BATCH_JOB_INSTANCE ji on ji.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID ";
    query +=
        " INNER JOIN BATCH_JOB_EXECUTION_PARAMS feed on feed.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID AND feed.KEY_NAME = '"
        + FeedConstants.PARAM__FEED_NAME + "' ";
    query +=
        " INNER JOIN BATCH_JOB_EXECUTION_PARAMS jobType on jobType.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID AND jobType.KEY_NAME = '"
        + FeedConstants.PARAM__JOB_TYPE + "' ";
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
