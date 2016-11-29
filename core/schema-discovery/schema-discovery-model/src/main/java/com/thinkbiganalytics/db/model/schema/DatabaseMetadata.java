package com.thinkbiganalytics.db.model.schema;

/**
 * Created by sr186054 on 2/17/16.
 */
@Deprecated
public class DatabaseMetadata {

    private String databaseName;
    private String tableName;
    private String columnName;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
}
