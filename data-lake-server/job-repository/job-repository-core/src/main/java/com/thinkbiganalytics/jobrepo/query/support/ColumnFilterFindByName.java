package com.thinkbiganalytics.jobrepo.query.support;

import com.google.common.base.Predicate;

/**
 * Created by sr186054 on 8/12/15.
 */
public class ColumnFilterFindByName implements Predicate<ColumnFilterItem> {
    private String name;

    public ColumnFilterFindByName(String name) {
        this.name = name;

    }

    @Override
    public boolean apply(ColumnFilterItem filter) {
        return name.equalsIgnoreCase(filter.getName());
    }
}

