package com.thinkbiganalytics.jobrepo.repository.dao;

import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.feed.*;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedFeed;
import com.thinkbiganalytics.jobrepo.query.model.ExecutionStatus;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.support.*;
import com.thinkbiganalytics.jobrepo.repository.FeedRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.dao.DataAccessException;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 8/13/15.
 */
@Named
public class FeedDao extends BaseQueryDao {
    Logger LOG = LoggerFactory.getLogger(FeedDao.class);


    public FeedQuery getQuery() {
        return getQuery(FeedQuery.class);
     }



    public List<Object> selectDistinctColumnValues(List<ColumnFilter> conditions, String column) {
        FeedQuery feedQuery = getQuery(FeedQuery.class);;
        feedQuery.setColumnFilterList(conditions);
        return selectDistinctColumnValues(feedQuery, column);
    }

    public Long selectCount(List<ColumnFilter> conditions) {
        FeedQuery feedQuery = getQuery(FeedQuery.class);;
        feedQuery.setColumnFilterList(conditions);
        return selectCount(feedQuery);
    }

    public List<ExecutedFeed> convertToExecutedFeed(List<JobExecution> jobExecutions) {
        List<ExecutedFeed> feeds = new ArrayList<ExecutedFeed>();
        for(JobExecution je: jobExecutions){
            ExecutedFeed executedFeed = FeedRepositoryImpl.convertToExecutedFeed(je);
            feeds.add(executedFeed);
        }
        return feeds;
    }

    public List<ExecutedFeed> findExecutedFeeds(List<ColumnFilter> conditions, List<OrderBy> order, final Integer start, final Integer limit) {
        List<ExecutedFeed> feeds = new ArrayList<ExecutedFeed>();
        FeedQuery feedQuery = getQuery(FeedQuery.class);
        feedQuery.setColumnFilterList(conditions);
        feedQuery.setOrderByList(order);
        try {
            List<JobExecution> jobExecutions = findList(feedQuery,start,limit);
           feeds = convertToExecutedFeed(jobExecutions);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return feeds;
    }



    public Map<ExecutionStatus,Long> getCountOfFeedsByStatus() {
        FeedStatusCountQuery query = new FeedStatusCountQuery(getDatabaseType());
        List<JobStatusCount> queryResult =  findList(query, 0, null);
        return DaoUtil.convertJobExecutionStatusCountResult(queryResult);
    }



    public Map<ExecutionStatus,Long> getCountOfLatestFeedsByStatus() {
        LatestFeedForStatusQuery query = new LatestFeedForStatusQuery(getDatabaseType());
        List<JobStatusCount> queryResult =  findList(query, 0, null);
        return DaoUtil.convertJobExecutionStatusCountResult(queryResult);
    }
    private Long getCountForStatus(FeedStatusCountQuery query, String status){
        Long count = 0L;
        List<ColumnFilter> filters = new ArrayList<>();
        filters.add(new QueryColumnFilterSqlString("STATUS",status));
        query.setColumnFilterList(filters);
        List<JobStatusCount> queryResult =  findList(query, 0, null);
        if(queryResult != null && !queryResult.isEmpty()){
            count = queryResult.get(0).getCount();
        }
        return count;
    }


    public Long getCountOfFeedsForStatus(String status){
        FeedStatusCountQuery query = new FeedStatusCountQuery(getDatabaseType());
        return getCountForStatus(query,status);

    }

    public Long getCountOfLatestJobsForStatus(String status) {
        LatestFeedStatusCountQuery query = new LatestFeedStatusCountQuery(getDatabaseType());
        return getCountForStatus(query,status);
    }

    public Map<String,Long> findAverageRunTimes() {
        Map<String,Long>avgRunTimes = new HashMap<String,Long>();
        FeedAverageRunTimesQuery query = getQuery(FeedAverageRunTimesQuery.class);
        List<Map<String,Object>>results = findList(query);
        if(results != null && !results.isEmpty()){
            for(Map<String,Object> result: results){
                String feed = (String)result.get("FEED_NAME");
                Double avg = (Double) result.get("AVG_RUN_TIME");
                avgRunTimes.put(feed,avg.longValue());
            }
        }
        return avgRunTimes;
    }


public List<String> getFeedNames(){
    List<String> feeds = new ArrayList<String>();
    String sql = "SELECT DISTINCT feed.STRING_VAL as FEED_NAME " +
            "FROM BATCH_JOB_EXECUTION e "+ FeedQueryUtil.feedQueryJoin("e", "feed");
    List<Map<String,Object>> feedList =  jdbcTemplate.queryForList(sql);
    for(Map<String,Object> feedMap : feedList) {
        feeds.add((String) feedMap.get("FEED_NAME"));
    }
    return feeds;
}


    public List<ExecutedFeed> findExecutedFeeds(AbstractConstructedQuery query){
        List<JobExecution> jobs = findList(query);
        return convertToExecutedFeed(jobs);
    }









}
