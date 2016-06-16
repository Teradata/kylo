package com.thinkbiganalytics.jobrepo.query.feed;


import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCountResult;
import com.thinkbiganalytics.jobrepo.query.support.FeedQueryUtil;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Returns a Count of each Feed, Status in the system
 */
public class FeedNameStatusCountQuery extends AbstractConstructedQuery {

  public FeedNameStatusCountQuery(DatabaseType databaseType) {
    super(databaseType);
  }

  public QueryBuilder getQueryBuilder() {
    QueryBuilder q = newQueryBuilder()
        .select("select count(*) CNT, feedName.STRING_VAL as FEED_NAME, e.STATUS ")
        .from("FROM  BATCH_JOB_EXECUTION e "
              + " " + FeedQueryUtil.feedQueryJoin("e", "feedName") + " ")
        .groupBy("STATUS, feedName.STRING_VAL");
    return q;
  }

  @Override
  public Query buildQuery() {
    return getQueryBuilder().build();
  }

  @Override
  public RowMapper<JobStatusCount> getRowMapper() {
    return new RowMapper<JobStatusCount>() {
      @Override
      public JobStatusCount mapRow(ResultSet resultSet, int i) throws SQLException {

        JobStatusCount jobStatusCount = new JobStatusCountResult();
        jobStatusCount.setFeedName(resultSet.getString("FEED_NAME"));
        jobStatusCount.setCount(resultSet.getLong("CNT"));
        jobStatusCount.setStatus(resultSet.getString("STATUS"));
        return jobStatusCount;
      }
    };
  }
}
