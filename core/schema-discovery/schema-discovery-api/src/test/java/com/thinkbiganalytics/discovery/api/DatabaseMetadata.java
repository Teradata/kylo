package com.thinkbiganalytics.discovery.api;

/**
 * Created by matthutton on 12/27/16.
 */
public interface DatabaseMetadata {

    String getDatabaseName();

    String getTableName();

    String getColumnName();
}
