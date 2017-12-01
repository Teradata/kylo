package com.thinkbiganalytics.ingest;

/*-
 * #%L
 * thinkbig-nifi-core-processors
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.jdbc.util.DatabaseUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides support for incremental
 */
public class GetTableDataSupport {

    private static final Logger logger = LoggerFactory.getLogger(GetTableDataSupport.class);

    private Connection conn;

    private int timeout;

    public GetTableDataSupport(Connection conn, int timeout) {
        Validate.notNull(conn);
        this.conn = conn;
        this.timeout = timeout;
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

    private String selectStatement(String[] selectFields, String tableAlias) {
        String dbIdentifierQuoteString = DatabaseUtils.getDatabaseIdentifierQuoteString(conn);
        List<String> list = new ArrayList<>();
        for (String field : selectFields) {
            String s = tableAlias + "." + dbIdentifierQuoteString + field + dbIdentifierQuoteString;
            list.add(s);
        }
        return StringUtils.join(list.toArray(), ",");
    }

    /**
     * Performs a full extract of the data for the specified table
     */
    public ResultSet selectFullLoad(String tableName, String[] selectFields) throws SQLException {
        final Statement st = conn.createStatement();
        st.setQueryTimeout(timeout);
        String query = getSelectQuery(tableName, selectFields);

        logger.info("Executing full GetTableData query {}", query);
        st.setQueryTimeout(timeout);

        return st.executeQuery(query);
    }

    private String getSelectQuery(String tableName, String[] selectFields) {
        String select = selectStatement(selectFields, "tbl");
        return "SELECT " + select + " FROM " + tableName + " " + "tbl";
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

        StringBuilder sb = new StringBuilder();
        String select = selectStatement(selectFields, "tbl");
        sb.append("select ").append(select).append(" from ").append(tableName).append(" tbl WHERE tbl.").append(dateField).append(" > ? and tbl.").append(dateField).append(" < ?");

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

    /**
     * Output format for table rows.
     */
    public enum OutputType {
        /**
         * Columns are separated by a delimiter
         */
        DELIMITED,

        /**
         * Each row is written as an Avro record
         */
        AVRO
    }

    public enum UnitSizes {
        NONE,
        HOUR,
        DAY,
        WEEK,
        MONTH,
        YEAR
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
