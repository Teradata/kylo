/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.jobrepo.query.job;


import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.DefaultQueryBuilder;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.model.DefaultRelatedJobExecution;
import com.thinkbiganalytics.jobrepo.query.model.RelatedJobExecution;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by sr186054 on 12/10/15.
 */
public class RelatedJobExecutionsQuery extends AbstractConstructedQuery {

private Long jobExecutionId;

    public RelatedJobExecutionsQuery() {
        super();
    }

    public RelatedJobExecutionsQuery(DatabaseType databaseType) {
        super(databaseType);
    }

    public RelatedJobExecutionsQuery(DatabaseType databaseType, Long jobExecutionId) {
        super(databaseType);
        this.jobExecutionId = jobExecutionId;
    }

    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    @Override
    public Query buildQuery() {
        return getQueryBuilder().build();
    }

    @Override
    public QueryBuilder getQueryBuilder() {
      return DefaultQueryBuilder.newQuery(getDatabaseType()).select("SELECT je.JOB_EXECUTION_ID, ji.JOB_NAME, je.START_TIME, je.END_TIME")
                .from("from BATCH_JOB_EXECUTION je\n" +
                        "inner join BATCH_JOB_INSTANCE ji on ji.JOB_INSTANCE_ID = je.JOB_INSTANCE_ID\n" +
                        "WHERE je.JOB_INSTANCE_ID = (SELECT JOB_INSTANCE_ID from BATCH_JOB_EXECUTION WHERE JOB_EXECUTION_ID = :executionId )")
                .withNamedParameter("executionId", jobExecutionId)
                .defaultOrderBy("JOB_EXECUTION_ID", "asc");

    }

    @Override
    public RowMapper getRowMapper() {
        return new RowMapper() {
            @Override
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                RelatedJobExecution row = new DefaultRelatedJobExecution();
                row.setJobExecutionId(resultSet.getLong("JOB_EXECUTION_ID"));
                row.setJobName(resultSet.getString("JOB_NAME"));
                row.setStartTime(resultSet.getTimestamp("START_TIME"));
                row.setEndTime(resultSet.getTimestamp("END_TIME"));
                return row;
            }
        };
    }
}
