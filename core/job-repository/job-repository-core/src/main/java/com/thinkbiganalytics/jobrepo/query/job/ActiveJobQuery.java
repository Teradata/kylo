package com.thinkbiganalytics.jobrepo.query.job;


import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.builder.DefaultQueryBuilder;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.QueryColumnFilterSqlString;

import java.util.List;

/**
 * Query to return the Latest (MAX) Job Executions which do not have an EXIT_CODE of NOOP along with any Job Executions that are
 * 'STARTED', or 'STARTING' <p> This is used for the Overview Page -> Active Jobs table
 */
public class ActiveJobQuery extends JobQuery {

  public ActiveJobQuery(DatabaseType databaseType) {
    super(databaseType);
  }


  protected List<ColumnFilter> getDefaultFilters() {
    List<ColumnFilter> filters = getColumnFilterList();
    filters.add(new QueryColumnFilterSqlString("STATUS", "STARTED,FAILED", "in"));
    return filters;
  }

  @Override
  public QueryBuilder newQueryBuilder() {
    List<ColumnFilter> filters = getDefaultFilters();
    if (this.getColumnFilterList() != null && !this.getColumnFilterList().isEmpty()) {
      filters.addAll(getColumnFilterList());
    }
    return DefaultQueryBuilder.newQuery(getDatabaseType()).withFilters(filters).orderBy(getOrderByList());

  }

  @Override
  public Query buildQuery() {
    return super.buildQuery();
  }

  @Override
  public QueryBuilder getQueryBuilder() {
    QueryBuilder q = super.getQueryBuilder();
    q.replaceFrom(" from (select je1.* from " +
                  "         BATCH_JOB_EXECUTION je1 " +
                  "               inner join ( " +
                  "                       select MAX(je.JOB_INSTANCE_ID) JOB_INSTANCE_ID, MAX(je.JOB_EXECUTION_ID) JOB_EXECUTION_ID "
                  +
                  "                                         FROM BATCH_JOB_EXECUTION je " +
                  "                                         inner join BATCH_JOB_INSTANCE ji on ji.JOB_INSTANCE_ID = je.JOB_INSTANCE_ID "
                  +
                  " WHERE je.STATUS NOT IN('NOOP','STARTING') " +
                  "    group by ji.JOB_NAME " +
                  " UNION SELECT JOB_INSTANCE_ID, JOB_EXECUTION_ID " +
                  " FROM BATCH_JOB_EXECUTION e " +
                  " WHERE e.STATUS in ('STARTED','STARTING') ) x on x.JOB_EXECUTION_ID = je1.JOB_EXECUTION_ID) e" +
                  getDefaultJoins());
    return q;
  }


}
