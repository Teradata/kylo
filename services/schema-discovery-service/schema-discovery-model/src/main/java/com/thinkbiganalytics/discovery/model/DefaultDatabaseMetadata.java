package com.thinkbiganalytics.discovery.model;

public class DefaultDatabaseMetadata implements com.thinkbiganalytics.discovery.schema.DatabaseMetadata {

    private String databaseName;
    private String tableName;
    private String columnName;

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

}
