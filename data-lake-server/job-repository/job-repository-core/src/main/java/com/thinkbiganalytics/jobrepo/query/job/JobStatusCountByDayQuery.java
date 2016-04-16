/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.jobrepo.query.job;


import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.ColumnFilterQueryModifier;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCountResult;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitutionFactory;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilterUtil;
import com.thinkbiganalytics.jobrepo.query.support.DaoUtil;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.support.OrderByClause;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Returns a count of each Feed and Status
 */
public class JobStatusCountByDayQuery extends AbstractConstructedQuery<JobStatusCount> {

  public JobStatusCountByDayQuery(DatabaseType databaseType) {
    super(databaseType);
  }

  public QueryBuilder getQueryBuilder() {
    QueryBuilder q = newQueryBuilder()
        .select("select count(*) CNT," + DaoUtil.getHealthStateSqlClause("e") + "as STATE," + DatabaseQuerySubstitutionFactory
            .getDatabaseSubstitution(getDatabaseType()).truncateTimestampToDate("e.START_TIME") + "as START_DATE")
        .from("FROM  BATCH_JOB_EXECUTION e ")
        .groupBy(DaoUtil.getHealthStateSqlClause("e") + "," + DatabaseQuerySubstitutionFactory
            .getDatabaseSubstitution(getDatabaseType()).truncateTimestampToDate("e.START_TIME"))
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
        ColumnFilterUtil.applyDatabaseTypeDateDiffSql(getDatabaseType(), columnFilter);

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
        statusCount.setStatus(resultSet.getString("STATE"));
        statusCount.setDate(new Date(resultSet.getDate("START_DATE").getTime()));
        return statusCount;
      }
    };
  }
}
