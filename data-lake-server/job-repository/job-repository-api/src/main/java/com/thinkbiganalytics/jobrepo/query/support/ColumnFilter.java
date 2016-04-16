package com.thinkbiganalytics.jobrepo.query.support;


import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface ColumnFilter extends ColumnFilterItem {
    List<ColumnFilterItem> getFilters();

    ColumnFilterItem getFirst();

    String getOperatorValueByName(String name);

    ColumnFilterItem getByName(String name);

    Object getValueForName(String name);

    String getValueAsStringForName(String name);

    Collection<?> getValueAsCollection(String name);

    boolean isSqlString();

    String getNameOrFirstFilterName();

    String getSqlString();

    void setSqlString(String sqlString);
}
