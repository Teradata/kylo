/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.jobrepo.query.job;


import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCountResult;
import com.thinkbiganalytics.jobrepo.query.support.DaoUtil;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class RunningOrFailedJobCountsQuery extends AbstractConstructedQuery<JobStatusCount> {

    public RunningOrFailedJobCountsQuery(DatabaseType databaseType) {
        super(databaseType);
    }

    public QueryBuilder getQueryBuilder(){
        QueryBuilder q = newQueryBuilder()
                .select("SELECT COUNT(*) CNT, "+ DaoUtil.getHealthStateSqlClause("e")+" as STATE ")
                .from("FROM  BATCH_JOB_EXECUTION e " +
                        "WHERE e.STATUS in ('FAILED','UNKNOWN','STARTED','STARTING') ")
                .groupBy( DaoUtil.getHealthStateSqlClause("e"));
        return q;
    }

    @Override
    public Query buildQuery(){
            return getQueryBuilder().build();
    }




    @Override
    public RowMapper<JobStatusCount> getRowMapper() {
        return new RowMapper<JobStatusCount>() {
            @Override
            public JobStatusCount mapRow(ResultSet resultSet, int i) throws SQLException {

                JobStatusCount statusCount = new JobStatusCountResult();
                statusCount.setCount(resultSet.getLong("CNT"));
                statusCount.setStatus(resultSet.getString("STATE"));
                return statusCount;
            }
        };
    }
}
