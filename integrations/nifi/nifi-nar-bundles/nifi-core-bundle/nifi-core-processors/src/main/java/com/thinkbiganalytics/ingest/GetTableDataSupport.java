package com.thinkbiganalytics.ingest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Date;

/**
 * Provides support for incremental
 */
public class GetTableDataSupport {

    public static Logger logger = LoggerFactory.getLogger(GetTableDataSupport.class);

    private Connection conn;

    private int timeout;

    /**
     * Output format for table rows.
     */
    public enum OutputType {
        /** Columns are separated by a delimiter */
        DELIMITED,

        /** Each row is written as an Avro record */
        AVRO
    }

    public enum UnitSizes {
        NONE,
        HOUR,
        DAY,
        WEEK,
        MONTH,
        YEAR;
    }

    protected static Date maxAllowableDateFromUnit(Date fromDate, UnitSizes unit) {
        DateTime jodaDate = new DateTime(fromDate);
        switch (unit) {
            case HOUR:
                return jodaDate.hourOfDay().roundFloorCopy().toDate();
            case DAY:
                return jodaDate.withHourOfDay(0).hourOfDay().roundFloorCopy().toDate();
            case WEEK:
                return jodaDate.weekOfWeekyear().roundFloorCopy().toDate();
            case MONTH:
                return jodaDate.monthOfYear().roundFloorCopy().toDate();
            case YEAR:
                return jodaDate.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).hourOfDay().roundFloorCopy().toDate();
        }
        return fromDate;
    }


    public GetTableDataSupport(Connection conn, int timeout) {
        Validate.notNull(conn);
        this.conn = conn;
    }

    private String selectStatement(String[] selectFields) {
        return StringUtils.join(selectFields, ",");
    }

    /**
     * Performs a full extract of the data for the specified table
     */
    public ResultSet selectFullLoad(String tableName, String[] selectFields) throws SQLException {
        final Statement st = conn.createStatement();
        st.setQueryTimeout(timeout);
        String select = selectStatement(selectFields);
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ").append(select).append(" FROM ").append(tableName);

        logger.info("Executing full GetTableData query {}", sb.toString());
        st.setQueryTimeout(timeout);
        final ResultSet resultSet = st.executeQuery(sb.toString());

        return resultSet;
    }

    /**
     * Provides an incremental select based on a date field and last status. The overlap time will be subtracted from
     * the last load date. This will cause duplicate records but also pickup records that were missed on the last scan
     * due to long-running transactions.
     *
     * @param tableName    the table
     * @param dateField    the name of the field containing last modified date used to perform the incremental load
     * @param overlapTime  the number of seconds to overlap with the last load status
     * @param lastLoadDate the last batch load date
     */
    public ResultSet selectIncremental(String tableName, String[] selectFields, String dateField, int overlapTime, Date lastLoadDate, int backoffTime, UnitSizes unit) throws SQLException {
        ResultSet rs = null;

        logger.info("selectIncremental tableName {} dateField {} overlapTime {} lastLoadDate {} backoffTime {} unit {}", tableName, dateField, overlapTime, lastLoadDate, backoffTime, unit.toString());

        final Date now = new Date(DateTimeUtils.currentTimeMillis());
        DateRange range = new DateRange(lastLoadDate, now, overlapTime, backoffTime, unit);

        logger.info("Load range with min {} max {}", range.getMinDate(), range.getMaxDate());

        StringBuffer sb = new StringBuffer();
        String select = selectStatement(selectFields);
        sb.append("select ").append(select).append(" from ").append(tableName).append(" WHERE " + dateField + " > ? and " + dateField + " < ?");

        if (range.getMinDate().before(range.getMaxDate())) {
            PreparedStatement ps = conn.prepareStatement(sb.toString());
            ps.setQueryTimeout(timeout);
            ps.setTimestamp(1, new java.sql.Timestamp(range.getMinDate().getTime()));
            ps.setTimestamp(2, new java.sql.Timestamp(range.getMaxDate().getTime()));

            logger.info("Executing incremental GetTableData query {}", ps);
            rs = ps.executeQuery();
        }
        return rs;
    }

    protected static class DateRange {
        private Date minDate;
        private Date maxDate;

        public DateRange(Date lastLoadDate, Date currentDate, int overlapTime, int backoffTime, UnitSizes unit) {

            lastLoadDate = (lastLoadDate == null ? new Date(0L) : lastLoadDate);
            this.minDate = new Date(lastLoadDate.getTime() - (Math.abs(overlapTime) * 1000L));

            // Calculate the max date
            Date maxLoadDate = (currentDate == null ? new Date() : currentDate);
            maxLoadDate = new Date(maxLoadDate.getTime() - Math.abs(backoffTime) * 1000L);
            this.maxDate = maxAllowableDateFromUnit(maxLoadDate, unit);
        }

        public Date getMaxDate() {
            return maxDate;
        }

        public Date getMinDate() {
            return minDate;
        }

        public String toString() {
            return "min (" + minDate + ") max(" + maxDate + ")";
        }

    }


}
