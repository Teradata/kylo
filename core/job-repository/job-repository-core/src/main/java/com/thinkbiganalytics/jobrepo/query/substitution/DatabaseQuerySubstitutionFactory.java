package com.thinkbiganalytics.jobrepo.query.substitution;


import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseUnsupportedException;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by sr186054 on 9/17/15.
 */
public class DatabaseQuerySubstitutionFactory {

  public static String FEED_EXECUTION_RUN_TIME_TEMPLATE_STRING = "${FEED_RUN_TIME}";
  public static String JOB_EXECUTION_RUN_TIME_TEMPLATE_STRING = "${JOB_RUN_TIME}";
  public static String AVG_JOB_EXECUTION_RUN_TIME_TEMPLATE_STRING = "${AVG_JOB_RUN_TIME}";


  public static DatabaseQuerySubstitution getDatabaseSubstitution(DatabaseType databaseType) {
    if (DatabaseType.POSTGRES.equals(databaseType)) {
      return new PostgresDatabaseQuerySubstitution();
    } else if (DatabaseType.MYSQL.equals(databaseType)) {
      return new MysqlDatabaseQuerySubstitution();
    } else {
      throw new DatabaseUnsupportedException();
    }
  }

  public static String applyQuerySubstitutions(String query, DatabaseType databaseType) {
    DatabaseQuerySubstitution querySubstitution = DatabaseQuerySubstitutionFactory.getDatabaseSubstitution(databaseType);
    query = StringUtils.replace(query, JOB_EXECUTION_RUN_TIME_TEMPLATE_STRING, querySubstitution.getJobExecutionRunTimeSql());
    query = StringUtils.replace(query, FEED_EXECUTION_RUN_TIME_TEMPLATE_STRING, querySubstitution.getFeedExecutionRunTimeSql());
    query = StringUtils.replace(query, AVG_JOB_EXECUTION_RUN_TIME_TEMPLATE_STRING, querySubstitution.getJobExecutionRunTimeSql());

    return query;
  }
}
