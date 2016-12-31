/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.metadata.MetadataField;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultTableSchema extends AbstractSchema implements TableSchema {

    @MetadataField
    private String schemaName;

    private String databaseName;

    @Override
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }


    @Override
    public String getDatabaseName() {
        return databaseName;
    }


    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}
