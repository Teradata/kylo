package com.thinkbiganalytics.jobrepo.repository;


import com.thinkbiganalytics.jobrepo.query.model.CheckDataJob;
import com.thinkbiganalytics.jobrepo.query.model.SearchResult;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;

import java.util.List;

/**
 * Created by sr186054 on 8/28/15.
 */
public interface CheckDataJobRepository {

    public SearchResult getDataTablesSearchResult(List<ColumnFilter> conditions, List<ColumnFilter> defaultFilters, List<OrderBy> order, Integer start, Integer limit);

    public List<Object> selectDistinctColumnValues(List<ColumnFilter> filters, String columnName);

    public Long selectCount(List<ColumnFilter> filters);

    public List<CheckDataJob> findLatestCheckDataJobs();

}
