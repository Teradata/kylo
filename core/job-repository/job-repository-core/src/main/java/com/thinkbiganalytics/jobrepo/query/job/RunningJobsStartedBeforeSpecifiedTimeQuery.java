/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.jobrepo.query.job;


import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitutionFactory;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.QueryColumnFilterSqlString;

import org.joda.time.DateTime;

import java.util.List;


public class RunningJobsStartedBeforeSpecifiedTimeQuery extends JobQuery {

    private DateTime startTime;

    public RunningJobsStartedBeforeSpecifiedTimeQuery(DatabaseType databaseType, DateTime startTime) {
        super(databaseType);
        this.startTime = startTime;
    }

    public RunningJobsStartedBeforeSpecifiedTimeQuery(DatabaseType databaseType) {
        super(databaseType);
    }

    protected List<ColumnFilter> getDefaultFilters() {
        List<ColumnFilter> filters = getColumnFilterList();
        filters.add(new QueryColumnFilterSqlString("STATUS", "STARTED,STARTING", "in"));
        filters.add(new QueryColumnFilterSqlString(" AND "+DatabaseQuerySubstitutionFactory.getDatabaseSubstitution(getDatabaseType()).dateTimeAsMillisecondsSql("e.START_TIME") + " <= " + startTime.getMillis()));
        return filters;
    }

    @Override
    public QueryBuilder getQueryBuilder() {
        QueryBuilder builder = super.getQueryBuilder();
        builder.withFilters(getDefaultFilters());
        return builder;
    }
}
