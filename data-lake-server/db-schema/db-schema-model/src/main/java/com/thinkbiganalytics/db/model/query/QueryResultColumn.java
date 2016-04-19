package com.thinkbiganalytics.db.model.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by sr186054 on 3/31/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryResultColumn {

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

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getHiveColumnLabel() {
        return hiveColumnLabel;
    }

    public void setHiveColumnLabel(String hiveColumnLabel) {
        this.hiveColumnLabel = hiveColumnLabel;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
