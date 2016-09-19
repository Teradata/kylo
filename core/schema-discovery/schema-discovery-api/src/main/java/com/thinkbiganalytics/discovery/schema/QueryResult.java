/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.schema;

import java.util.List;
import java.util.Map;

public interface QueryResult {

    List<? extends QueryResultColumn> getColumns();

    boolean isEmpty();

    void setColumns(List<? extends QueryResultColumn> columns);

    List<Map<String,Object>> getRows();

    void addRow(Map<String, Object> data);

    String getQuery();

    Map<String, ? extends QueryResultColumn> getColumnFieldMap();

    Map<String, ? extends QueryResultColumn> getColumnDisplayNameMap();
}
