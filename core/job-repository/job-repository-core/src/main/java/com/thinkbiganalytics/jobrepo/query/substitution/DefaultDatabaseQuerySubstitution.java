package com.thinkbiganalytics.jobrepo.query.substitution;

/**
 * Created by sr186054 on 9/17/15.
 */
public class DefaultDatabaseQuerySubstitution implements DatabaseQuerySubstitution {

  public String getDateDiffWhereClause(String column, DATE_PART dayPart, Integer number) {
    return null;
  }

  public String truncateTimestampToDate(String column) {
    return "trunc(" + column + ")";
  }


  public String getJobExecutionRunTimeSql() {
    return "TIMESTAMPDIFF(SECOND,e.START_TIME,COALESCE(e.END_TIME,NOW()))";
  }

  public String getTimeAfterNow(DATE_PART datePart, Integer number) {
    return null;
  }

  public String getTimeBeforeNow(DATE_PART datePart, Integer number) {
    return null;
  }

  public String getTimeSinceEndTimeSql(String tableAlias) {
    return "NOW() - "+tableAlias+".END_TIME";
  }

  @Override
  public String getFeedExecutionRunTimeSql() {
    return null;
  }

  @Override
  public String limitAndOffset(Integer limit, Integer offset) {
    String limitString = "";
    return limitString;
  }

  public String toDateSql(String date) {
    return "TO_DATE('" + date + "','MM/DD/YYYY')";
  }
}
