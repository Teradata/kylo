package com.thinkbiganalytics.nifi.thrift.api;

import java.sql.ResultSet;
import java.util.Date;

/**
 * Intercept iterations over a resultset
 */
public interface RowVisitor {

    void visitRow(ResultSet row);

    void visitColumn(String columnName, int colType, Date value);

    void visitColumn(String columnName, int colType, String value);
}
