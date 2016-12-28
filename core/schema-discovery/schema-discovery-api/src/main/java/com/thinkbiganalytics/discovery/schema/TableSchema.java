/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.schema;

/**
 * Schema of a table
 */
public interface TableSchema extends Schema {

    /**
     * Name of the table schema
     */
    String getTableSchemaName();

    /**
     * Database name
     */
    String getDatabaseName();

}
