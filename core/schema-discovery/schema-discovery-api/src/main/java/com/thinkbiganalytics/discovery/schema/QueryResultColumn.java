package com.thinkbiganalytics.discovery.schema;


public interface QueryResultColumn {

    String getDataType();

    void setDataType(String dataType);

    String getTableName();

    void setTableName(String tableName);

    String getDatabaseName();

    void setDatabaseName(String databaseName);

    String getDisplayName();

    void setDisplayName(String displayName);

    String getHiveColumnLabel();

    void setHiveColumnLabel(String hiveColumnLabel);

    int getIndex();

    void setIndex(int index);

    String getField();

    void setField(String field);
}
