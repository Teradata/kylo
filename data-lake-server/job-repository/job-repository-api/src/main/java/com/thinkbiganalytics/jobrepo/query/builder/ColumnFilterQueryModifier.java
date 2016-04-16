package com.thinkbiganalytics.jobrepo.query.builder;

import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;

/**
 * Modify the ColumnFilters before they get applied to the SQL
 * A place to translate the incoming Filter String to a SQL Query String.
 * (i.e. user filters on RUN_TIME, the query needs to translate that into the Query
 * specific clause to calculate and do a WHERE Clause on RUN_TIME
 */
public interface ColumnFilterQueryModifier {


    public void modifyFilterQueryValue(ColumnFilter columnFilter);
}
