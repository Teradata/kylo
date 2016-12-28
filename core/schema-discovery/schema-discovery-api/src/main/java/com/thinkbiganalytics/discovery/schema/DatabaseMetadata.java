/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.schema;

public interface DatabaseMetadata {

    String getDatabaseName();

    String getTableName();

    String getColumnName();

}
