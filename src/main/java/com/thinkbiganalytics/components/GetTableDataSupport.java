/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.components;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
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
     * @param tableName   the table
     * @param dateField   the name of the field containing last modified date used to perform the incremental load
     * @param overlapTime the number of seconds to overlap with the last load status
     * @param lastLoadDate  the last batch load date
     */
    public ResultSet selectIncremental(String tableName, String[] selectFields, String dateField, int overlapTime, Date lastLoadDate) throws SQLException {
        ResultSet rs;

        logger.info("selectIncremental tableName {} dateField {} overlapTime {} lastLoadDate {}", tableName, dateField, overlapTime, lastLoadDate);

        if (lastLoadDate == null) {
            logger.info("LastLoadDate is empty, returning full data");
            return selectFullLoad(tableName, selectFields);
        }

        Validate.notEmpty(dateField, "Expecting a date field");
        overlapTime = Math.abs(overlapTime);

        Date nextLoadStart = new Date(lastLoadDate.getTime() - (overlapTime * 1000));
        logger.info("Load time with overlap {}", nextLoadStart);

        StringBuffer sb = new StringBuffer();
        String select = selectStatement(selectFields);
        sb.append("select ").append(select).append(" from ").append(tableName).append(" WHERE " + dateField + " > ?");

        PreparedStatement ps = conn.prepareStatement(sb.toString());
        ps.setQueryTimeout(timeout);
        ps.setTimestamp(1, new java.sql.Timestamp(nextLoadStart.getTime()));

        logger.info("Executing incremental GetTableData query {}", ps);
        rs = ps.executeQuery();
        return rs;

    }

}
