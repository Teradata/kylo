/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.discovery.schema.TableSchema;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultTableSchema extends AbstractSchema implements TableSchema {

    private String tableSchema;
    private String databaseName;

    @Override
    public String getTableSchemaName() {
        return tableSchema;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}
