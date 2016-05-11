package com.thinkbiganalytics.jobrepo.query;


import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryFactory;
import com.thinkbiganalytics.jobrepo.query.feed.FeedAverageRunTimesQuery;
import com.thinkbiganalytics.jobrepo.query.feed.FeedHealthCheckDataQuery;
import com.thinkbiganalytics.jobrepo.query.feed.FeedHealthQuery;
import com.thinkbiganalytics.jobrepo.query.feed.FeedNameStatusCountQuery;
import com.thinkbiganalytics.jobrepo.query.feed.FeedQuery;
import com.thinkbiganalytics.jobrepo.query.feed.FeedQueryConstants;
import com.thinkbiganalytics.jobrepo.query.feed.FeedQueryWithLatestFeedColumn;
import com.thinkbiganalytics.jobrepo.query.feed.LatestCompletedFeedQuery;
import com.thinkbiganalytics.jobrepo.query.feed.LatestFeedNotStoppedQuery;
import com.thinkbiganalytics.jobrepo.query.feed.LatestFeedStatusCountQuery;
import com.thinkbiganalytics.jobrepo.query.feed.LatestOperationalFeedQuery;
import com.thinkbiganalytics.jobrepo.query.feed.MaxFeedQuery;
import com.thinkbiganalytics.jobrepo.query.job.JobQuery;
import com.thinkbiganalytics.jobrepo.query.job.RunningJobsStartedBeforeSpecifiedTimeQuery;
import com.thinkbiganalytics.jobrepo.query.model.FeedHealth;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;
import com.thinkbiganalytics.jobrepo.query.support.OrderByClause;
import com.thinkbiganalytics.jobrepo.query.support.QueryColumnFilterSqlString;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;

import java.util.ArrayList;
import java.util.List;

import javax.batch.runtime.BatchStatus;

/**
 * Created by sr186054 on 9/24/15.
 */
public class FeedQueryTest {

    @Test
    public void testQueries() {
        testFeedQuery(DatabaseType.MYSQL, FeedQuery.class);
        testFeedQuery(DatabaseType.POSTGRES, FeedQuery.class);
        testFeedQuery(DatabaseType.MYSQL, FeedQueryWithLatestFeedColumn.class);
        testFeedQuery(DatabaseType.POSTGRES, FeedQueryWithLatestFeedColumn.class);
        testQuery(DatabaseType.MYSQL, LatestFeedNotStoppedQuery.class);
        testQuery(DatabaseType.MYSQL, LatestFeedStatusCountQuery.class);
        testQuery(DatabaseType.MYSQL, FeedAverageRunTimesQuery.class);
        testQuery(DatabaseType.MYSQL,FeedNameStatusCountQuery.class);
    }

    @Test
    public void testLatestCompletedFeedQuery() {
        testFeedQuery(DatabaseType.MYSQL, LatestCompletedFeedQuery.class);
        testFeedQuery(DatabaseType.POSTGRES, LatestCompletedFeedQuery.class);
    }

    @Test
    public void testFeedQueryWithLatestFeedColumn(){
        testFeedQuery(DatabaseType.MYSQL, FeedQuery.class);
        testFeedQuery(DatabaseType.MYSQL, LatestOperationalFeedQuery.class);
    }

    @Test
    public void testRunningJobsStartedBeforeSpecifiedTimeQuery(){
        RunningJobsStartedBeforeSpecifiedTimeQuery query = new RunningJobsStartedBeforeSpecifiedTimeQuery(DatabaseType.POSTGRES,new DateTime());
        Query q = query.buildQuery();
        logQuery(q, RunningJobsStartedBeforeSpecifiedTimeQuery.class);

        query = new RunningJobsStartedBeforeSpecifiedTimeQuery(DatabaseType.MYSQL,new DateTime());
         q = query.buildQuery();
        logQuery(q, RunningJobsStartedBeforeSpecifiedTimeQuery.class);
    }

    @Test
    public void testCheckDataQuery(){
        testQuery(DatabaseType.POSTGRES, FeedHealthQuery.class);
    }

    @Test
    public void test2(){
        testQuery(DatabaseType.MYSQL, JobQuery.class);
    }

    @Test
    public void testAverageRunTimeQuery(){
        testQuery(DatabaseType.MYSQL, FeedAverageRunTimesQuery.class);
        testQuery(DatabaseType.POSTGRES, FeedAverageRunTimesQuery.class);
    }
    @Test
    public void testNameStatusCountQuery(){
        testQuery(DatabaseType.MYSQL, FeedNameStatusCountQuery.class);
        testQuery(DatabaseType.POSTGRES, FeedNameStatusCountQuery.class);
    }

    @Test
    public void findLastCompletedFeeds( ) {
        LatestCompletedFeedQuery query = new LatestCompletedFeedQuery(DatabaseType.MYSQL);
        List<OrderBy> orderBy = new ArrayList<>();
        orderBy.add(new OrderByClause(FeedQueryConstants.QUERY_FEED_NAME_COLUMN,"asc"));
        query.setOrderByList(orderBy);
        Query q = query.buildQuery();
        logQuery(q, LatestCompletedFeedQuery.class);

    }

    @Test
    public void findLastCompletedFeed( ) {
        List<ColumnFilter> filter = new ArrayList<ColumnFilter>();
        filter.add(new QueryColumnFilterSqlString(FeedQueryConstants.QUERY_FEED_NAME_COLUMN, "ADSB"));
        LatestCompletedFeedQuery query = new LatestCompletedFeedQuery(DatabaseType.MYSQL);
        query.setColumnFilterList(filter);
        Query q = query.buildQuery();
        logQuery(q, LatestCompletedFeedQuery.class);

    }

    @Test
    public void testMaxFeedQuery(){
        MaxFeedQuery query = new MaxFeedQuery(DatabaseType.MYSQL);
        List<ColumnFilter> filters = new ArrayList<ColumnFilter>();
        filters.add(new QueryColumnFilterSqlString("STATUS", BatchStatus.COMPLETED));
        filters.add(new QueryColumnFilterSqlString("EXIT_CODE", ExitStatus.COMPLETED));
        query.setColumnFilterList(filters);
        Query q = query.buildQuery();
        logQuery(q, MaxFeedQuery.class);
    }






    private void testQuery(DatabaseType databaseType,Class<? extends AbstractConstructedQuery> clazz){
        AbstractConstructedQuery query = QueryFactory.getQuery(databaseType, clazz);
        Query q = query.buildQuery();
        logQuery(q, clazz);
    }
    private void logQuery(Query q, Class clazz){
        System.out.println(clazz.getSimpleName() + " " + q.getDatabaseType());
        System.out.println(q.getQuery());
        System.out.println(q.getNamedParameters());
    }



    private void testFeedQuery(DatabaseType databaseType, Class<? extends FeedQuery> clazz) {
        FeedQuery query = QueryFactory.getQuery(databaseType, clazz);
        List<ColumnFilter> filters = new ArrayList<ColumnFilter>();
        //  filters.add(new ColumnFilter("STATUS", BatchStatus.COMPLETED.name()));
        //  filters.add(new ColumnFilter(FeedQuery.QUERY_FEED_NAME_COLUMN, "testFeed"));
        query.setColumnFilterList(filters);
        List<OrderBy> order = new ArrayList<>();
        order.add(new OrderByClause(FeedQuery.QUERY_RUN_TIME, "ASC"));
        query.setOrderByList(order);
        Query q = query.buildQuery();
        logQuery(q, clazz);
    }



}
