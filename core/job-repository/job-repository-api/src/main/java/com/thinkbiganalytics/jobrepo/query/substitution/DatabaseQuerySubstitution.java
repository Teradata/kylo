package com.thinkbiganalytics.jobrepo.query.substitution;

/**
 * Time calculations vary across databases. Because of this these unique strings are passed in via implementers of this interface
 */
public interface DatabaseQuerySubstitution {

  public static enum DATE_PART {
    DAY, WEEK, MONTH, YEAR;

    public String plural() {
      return this.name() + "s";
    }
  }

  public String truncateTimestampToDate(String column);

  public String toDateSql(String date);

  public String getDateDiffWhereClause(String column, DATE_PART dayPart, Integer number);

  /**
   * Return a SQL fragment that will return The Current Date - some Interval
   */
  public String getTimeBeforeNow(DATE_PART datePart, Integer number);

  public String getTimeAfterNow(DATE_PART datePart, Integer number);

  public String getTimeSinceEndTimeSql(String tableAlias);

  public String getJobExecutionRunTimeSql();

  public String getFeedExecutionRunTimeSql();

  public String limitAndOffset(Integer limit, Integer offset);
}
