package com.thinkbiganalytics.jobrepo.query.substitution;

/**
 * Created by sr186054 on 9/17/15.
 */
public class PostgresDatabaseQuerySubstitution implements DatabaseQuerySubstitution {

  public String getDateDiffWhereClause(String column, DATE_PART datePart, Integer number) {
    if (datePart != null) {
      switch (datePart) {
        case DAY:
          return " DATE_PART('day', (NOW() - e.START_TIME) ) <= " + number;
        case WEEK:
          return " TRUNC(DATE_PART('day', NOW() - e.START_TIME)/7) <= " + number;
        case MONTH:
          return
              " (DATE_PART('year', NOW()) - DATE_PART('year', e.START_TIME) ) *12 + (DATE_PART('month', NOW()) - DATE_PART('month', e.START_TIME)) <= "
              + number;
        case YEAR:
          return " DATE_PART('year', NOW()) - DATE_PART('year', e.START_TIME) <= " + number;
      }
    }
    return "";
  }

  /**
   * Return a SQL fragment that will return The Current Date - some Interval
   */
  public String getTimeBeforeNow(DATE_PART datePart, Integer number) {
    if (datePart != null) {
      return " NOW() - INTERVAL '" + number + " " + datePart.plural() + "' ";
    }
    return "";
  }

  /**
   * Return a SQL fragment that will return The Current Date - some Interval
   */
  public String getTimeAfterNow(DATE_PART datePart, Integer number) {
    if (datePart != null) {
      return " NOW() + INTERVAL '" + number + " " + datePart.plural() + "' ";
    }
    return "";
  }

  public String truncateTimestampToDate(String column) {
    return " date_trunc('day'," + column + ")";

  }


  public String getJobExecutionRunTimeSql() {
    return "EXTRACT(EPOCH FROM (COALESCE(e.END_TIME,NOW()) - e.START_TIME )) ";
  }

  @Override
  public String getFeedExecutionRunTimeSql() {
    return "EXTRACT(EPOCH FROM (COALESCE(childJobs.END_TIME,COALESCE(e.END_TIME,NOW())) - e.START_TIME )) ";
  }

  public String getTimeSinceEndTimeSql(String tableAlias) {
    return  "EXTRACT(EPOCH FROM (NOW() - COALESCE("+tableAlias+".END_TIME,NOW()))) ";
  }

  @Override
  public String limitAndOffset(Integer limit, Integer offset) {
    String limitString = "";
    if (limit != null && limit > 0) {
      limitString = " LIMIT " + limit;
      if (offset != null) {
        limitString += " OFFSET " + offset;
      }
    }
    return limitString;
  }

  public String toDateSql(String date) {
    return "TO_DATE('" + date + "','MM/DD/YYYY')";
  }
}
