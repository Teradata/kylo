/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Intercept iterations over a resultset
 */
public interface RowVisitor {

    void visitRow(ResultSet row);

    void visitColumn(String columnName, int colType, Date value);

    void visitColumn(String columnName, int colType, String value);
}
