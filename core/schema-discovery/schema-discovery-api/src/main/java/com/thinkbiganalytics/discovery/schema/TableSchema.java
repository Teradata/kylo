package com.thinkbiganalytics.discovery.schema;

/**
 * Schema of a table object
 */
public interface TableSchema extends Schema {

    /**
     * Name of the table schema
     */
    String getSchemaName();

    /**
     * Database name
     */
    String getDatabaseName();

}
