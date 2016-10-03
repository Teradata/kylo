package com.thinkbiganalytics.jobrepo.query.job;


import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.builder.ColumnFilterQueryModifier;
import com.thinkbiganalytics.jobrepo.query.builder.DefaultQueryBuilder;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;

import java.util.List;

/**
 * Shows just the Latest JobExeuctions of the Check Data Jobs where the status != ABANDONED
 */
public class CheckDataLatestJobsQuery extends CheckDataAllJobsQuery {

  private List<ColumnFilter> selectMaxFilters;


  @Override
  public QueryBuilder getQueryBuilder() {
    QueryBuilder q = super.getQueryBuilder();
    q.replaceFrom(" BATCH_JOB_EXECUTION e" +
                  " LEFT JOIN BATCH_JOB_INSTANCE ji on ji.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID " +
                  " LEFT JOIN BATCH_JOB_EXECUTION_CTX_VALS exec_ctx1 ON exec_ctx1.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID " +
                  " AND exec_ctx1.KEY_NAME = :validation_key " +
                  " LEFT JOIN BATCH_JOB_EXECUTION_CTX_VALS exec_ctx2 ON exec_ctx2.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID " +
                  " AND exec_ctx2.KEY_NAME = :validation_message_key ")
        .innerJoin(getMaxQuery()).as("p2 ").on("p2.MAX_JOB_EXECUTION_ID = e.JOB_EXECUTION_ID");
    return q;
  }

  private Query getMaxQuery() {
    return DefaultQueryBuilder.newQuery(getDatabaseType())
        .select("select params.STRING_VAL, MAX(je.JOB_EXECUTION_ID) MAX_JOB_EXECUTION_ID, jobType.STRING_VAL as JOB_TYPE ")
        .from(" BATCH_JOB_EXECUTION je " +
              " INNER JOIN BATCH_JOB_EXECUTION_PARAMS jobType on jobType.JOB_EXECUTION_ID = je.JOB_EXECUTION_ID " +
              " and jobType.KEY_NAME = :jobType " +
              " and UPPER(jobType.STRING_VAL) = :check " +
              " INNER JOIN BATCH_JOB_EXECUTION_PARAMS params on params.JOB_EXECUTION_ID = je.JOB_EXECUTION_ID " +
              " and params.KEY_NAME =  :feed ")
        .withFilters(selectMaxFilters)
        .groupBy("params.STRING_VAL, jobType.STRING_VAL ")
        .buildWithFilterQueryModifier(new ColumnFilterQueryModifier() {
          @Override
          public void modifyFilterQueryValue(ColumnFilter columnFilter) {
            columnFilter.setTableAlias("je");
          }
        });
  }

  public CheckDataLatestJobsQuery(DatabaseType databaseType) {
    super(databaseType);
  }


  public void setSelectMaxFilters(List<ColumnFilter> selectMaxFilters) {
    this.selectMaxFilters = selectMaxFilters;
  }
}
