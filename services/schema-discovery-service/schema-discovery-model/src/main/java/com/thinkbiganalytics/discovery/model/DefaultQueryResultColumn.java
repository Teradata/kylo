/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.discovery.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;

/**
 * Created by sr186054 on 3/31/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultQueryResultColumn implements QueryResultColumn {

    private String displayName;
    private String hiveColumnLabel;
    private String field;
    private String dataType;
    private String tableName;
    private String databaseName;
    private int index;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Override
    public String getDataType() {
        return dataType;
    }

    @Override
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getHiveColumnLabel() {
        return hiveColumnLabel;
    }

    @Override
    public void setHiveColumnLabel(String hiveColumnLabel) {
        this.hiveColumnLabel = hiveColumnLabel;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }
}
