package com.thinkbiganalytics.jobrepo.repository;


import com.thinkbiganalytics.jobrepo.query.model.CheckDataJob;
import com.thinkbiganalytics.jobrepo.query.model.SearchResult;
import com.thinkbiganalytics.jobrepo.query.model.SearchResultImpl;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;
import com.thinkbiganalytics.jobrepo.repository.dao.CheckDataJobDao;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by sr186054 on 8/28/15.
 */
@Named
public class CheckDataJobRepositoryImpl implements CheckDataJobRepository {

  @Inject
  CheckDataJobDao checkDataJobDao;

  public SearchResult getDataTablesSearchResult(List<ColumnFilter> conditions, List<ColumnFilter> defaultFilters,
                                                List<OrderBy> order, Integer start, Integer limit) {
    if (defaultFilters != null && !defaultFilters.isEmpty()) {
      if (conditions == null) {
        conditions = new ArrayList<ColumnFilter>();
      }
      conditions.addAll(defaultFilters);
    }
    List<CheckDataJob> jobs = checkDataJobDao.findCheckDataJobs(conditions, order, start, limit);
    Long filterCount = checkDataJobDao.selectCount(conditions);
    Long allCount = checkDataJobDao.selectCount(defaultFilters);
    SearchResult searchResult = new SearchResultImpl();
    searchResult.setData(jobs);
    searchResult.setRecordsFiltered(filterCount);
    searchResult.setRecordsTotal(allCount);
    return searchResult;
  }

  public List<Object> selectDistinctColumnValues(List<ColumnFilter> filters, String columnName) {
    List<Object> columnValues = new ArrayList<Object>();
    columnValues = checkDataJobDao.selectDistinctColumnValues(filters, columnName);
    return columnValues;
  }

  public Long selectCount(List<ColumnFilter> filters) {
    return checkDataJobDao.selectCount(filters);
  }

  public List<CheckDataJob> findLatestCheckDataJobs() {
    return checkDataJobDao.findLatestCheckDataJobs();
  }


}
