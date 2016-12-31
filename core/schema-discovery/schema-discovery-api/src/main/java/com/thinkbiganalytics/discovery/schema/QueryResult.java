/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.schema;

import java.util.List;
import java.util.Map;

public interface QueryResult {

    List<QueryResultColumn> getColumns();

    boolean isEmpty();

    void setColumns(List<QueryResultColumn> columns);

    List<Map<String,Object>> getRows();

    void addRow(Map<String, Object> data);

    String getQuery();

    Map<String,QueryResultColumn> getColumnFieldMap();

    Map<String, QueryResultColumn> getColumnDisplayNameMap();
}
