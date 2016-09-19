/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.schema;

/**
 * Represents a Hive table
 */
public interface HiveTableSchema extends TableSchema {

    String getHiveFormat();

    void setHiveFormat(String hiveFormat);

}
