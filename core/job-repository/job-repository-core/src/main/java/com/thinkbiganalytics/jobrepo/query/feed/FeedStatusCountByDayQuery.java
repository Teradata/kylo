/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.jobrepo.query.feed;


import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.ColumnFilterQueryModifier;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCountResult;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitutionFactory;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilterUtil;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.support.FeedQueryUtil;
import com.thinkbiganalytics.jobrepo.query.support.OrderByClause;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Returns a count of each Feed and Status
 */
public class FeedStatusCountByDayQuery extends AbstractConstructedQuery<JobStatusCount> {

  public FeedStatusCountByDayQuery(DatabaseType databaseType) {
    super(databaseType);
  }

  public QueryBuilder getQueryBuilder() {
    QueryBuilder q = newQueryBuilder()
        .select("select count(*) CNT, e.STATUS, " + DatabaseQuerySubstitutionFactory.getDatabaseSubstitution(getDatabaseType())
            .truncateTimestampToDate("e.START_TIME") + " as START_DATE ")
        .from("FROM  BATCH_JOB_EXECUTION e "
              + " " + FeedQueryUtil.feedQueryJoin("e", "feedName", false) + " ")
        .groupBy("e.STATUS," + DatabaseQuerySubstitutionFactory.getDatabaseSubstitution(getDatabaseType())
            .truncateTimestampToDate("e.START_TIME"))
        .orderBy(new OrderByClause(
            DatabaseQuerySubstitutionFactory.getDatabaseSubstitution(getDatabaseType()).truncateTimestampToDate("e.START_TIME"),
            "asc"));
    return q;
  }

  @Override
  public Query buildQuery() {
    return getQueryBuilder().buildWithFilterQueryModifier(new ColumnFilterQueryModifier() {
      @Override
      public void modifyFilterQueryValue(ColumnFilter columnFilter) {
        String name = columnFilter.getName();
        String strVal = columnFilter.getStringValue();
        if (("STRING_VAL".equals(name)
             || (FeedQueryConstants.QUERY_FEED_NAME_COLUMN.equals(name)) && !FeedQueryConstants.QUERY_ALL_VALUE
            .equalsIgnoreCase(strVal))) {
          columnFilter.setQueryName("STRING_VAL");
          columnFilter.setTableAlias("feedName");
        } else {
          ColumnFilterUtil.applyDatabaseTypeDateDiffSql(getDatabaseType(), columnFilter);
        }
      }
    });
  }


  @Override
  public RowMapper<JobStatusCount> getRowMapper() {
    return new RowMapper<JobStatusCount>() {
      @Override
      public JobStatusCount mapRow(ResultSet resultSet, int i) throws SQLException {

        JobStatusCount statusCount = new JobStatusCountResult();
        statusCount.setCount(resultSet.getLong("CNT"));
        statusCount.setStatus(resultSet.getString("STATUS"));
        statusCount.setDate(new Date(resultSet.getDate("START_DATE").getTime()));
        return statusCount;
      }
    };
  }
}
