package com.thinkbiganalytics.jobrepo.repository.dao;

import com.thinkbiganalytics.jobrepo.query.job.CheckDataAllJobsQuery;
import com.thinkbiganalytics.jobrepo.query.job.CheckDataLatestJobsQuery;
import com.thinkbiganalytics.jobrepo.query.model.CheckDataJob;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;
import com.thinkbiganalytics.jobrepo.query.support.QueryColumnFilterSqlString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.Assert;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 8/13/15.
 */
@Named
public class CheckDataJobDao extends BaseQueryDao {
    Logger LOG = LoggerFactory.getLogger(CheckDataJobDao.class);


    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.namedParameterJdbcTemplate);
    }

    public CheckDataAllJobsQuery getQuery(){
        return new CheckDataAllJobsQuery(getDatabaseType());
    }

    public CheckDataLatestJobsQuery getLatestJobsQuery(){
        return new CheckDataLatestJobsQuery(getDatabaseType());
    }

    public List<Object> selectDistinctColumnValues(List<ColumnFilter> conditions, String column) {
        CheckDataAllJobsQuery query = getQuery();
        query.setColumnFilterList(conditions);
        return selectDistinctColumnValues(query, column);
    }

    public Long selectCount(List<ColumnFilter> conditions) {
        CheckDataAllJobsQuery query = getQuery();
        query.setColumnFilterList(conditions);
        return selectCount(query);
    }

    public List<CheckDataJob> findCheckDataJobs(List<ColumnFilter> conditions, List<OrderBy> order, final Integer start, final Integer limit) {
        List<CheckDataJob> feeds = new ArrayList<CheckDataJob>();
        CheckDataAllJobsQuery query = getQuery();
        query.setColumnFilterList(conditions);
        query.setOrderByList(order);
        try {
            feeds = findList(query, start, limit);

        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return feeds;
    }

    public List<CheckDataJob> findLatestCheckDataJobs() {
        List<CheckDataJob> jobs = new ArrayList<CheckDataJob>();
        CheckDataLatestJobsQuery query = getLatestJobsQuery();
        //Filter out those jobs that are currently running
        List<ColumnFilter> filters = new ArrayList<>();
        filters.add(new QueryColumnFilterSqlString("STATUS", "STARTING,STARTED", "NOT IN"));
        query.setSelectMaxFilters(filters);
        try {
            jobs = findList(query, 0, null);

        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return jobs;
    }



}
