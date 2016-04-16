package com.thinkbiganalytics.jobrepo.query.support;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface OrderBy {
    String getColumnName();

    void setColumnName(String columnName);

    String getDir();

    void setDir(String dir);

    String getQueryName();

    void setQueryName(String queryName);

    String getTableAlias();

    void setTableAlias(String tableAlias);
}
