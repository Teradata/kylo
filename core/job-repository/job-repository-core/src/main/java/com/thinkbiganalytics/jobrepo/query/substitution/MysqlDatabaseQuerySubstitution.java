package com.thinkbiganalytics.jobrepo.query.substitution;

/**
 * Created by sr186054 on 9/17/15.
 */
public class MysqlDatabaseQuerySubstitution implements DatabaseQuerySubstitution {

  public String truncateTimestampToDate(String column) {
    return "date(" + column + ")";
  }

  public String getDateDiffWhereClause(String column, DATE_PART datePart, Integer number) {
    return " " + column + " >= DATE_SUB(NOW(),INTERVAL " + number + " " + datePart.name() + ") ";
  }

  /**
   * Return a SQL fragment that will return The Current Date - some Interval
   */
  public String getTimeBeforeNow(DATE_PART datePart, Integer number) {
    return "DATE_SUB(NOW(),INTERVAL " + number + " " + datePart.name() + ") ";
  }


  public String getTimeAfterNow(DATE_PART datePart, Integer number) {
    return "DATE_ADD(NOW(),INTERVAL " + number + " " + datePart.name() + ") ";
  }

  public String getJobExecutionRunTimeSql() {
    return "TIMESTAMPDIFF(SECOND,e.START_TIME,COALESCE(e.END_TIME,NOW()))";
  }

  public String getTimeSinceEndTimeSql(String tableAlias) {
    return "TIMESTAMPDIFF(SECOND,COALESCE("+tableAlias+".END_TIME,NOW()),NOW())";
  }



  @Override
  public String getFeedExecutionRunTimeSql() {
    return "TIMESTAMPDIFF(SECOND,e.START_TIME,COALESCE(childJobs.END_TIME,COALESCE(e.END_TIME,NOW())))";
  }

  public String toDateSql(String date) {
    return "STR_TO_DATE('" + date + "','%m/%d/%Y')";
  }

  @Override
  public String limitAndOffset(Integer limit, Integer offset) {
    String limitString = "";
    if (limit != null && limit > 0) {
      if (offset != null) {
        limitString += " LIMIT " + offset + "," + limit;
      } else {
        limitString = "LIMIT " + limit;
      }

    }
    return limitString;
  }
}
