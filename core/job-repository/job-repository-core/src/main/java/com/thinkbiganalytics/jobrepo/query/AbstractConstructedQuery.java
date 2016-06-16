package com.thinkbiganalytics.jobrepo.query;


import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.builder.DefaultQueryBuilder;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilterUtil;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;

import org.springframework.jdbc.core.RowMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract version on a Constructed Query Needs to supply the QueryBuilder and how the Query is built and Mapped upon return
 */
public abstract class AbstractConstructedQuery<T> implements ConstructedQuery<T> {

  private List<ColumnFilter> columnFilterList;
  private List<OrderBy> orderByList;

  private DatabaseType databaseType;


  public abstract RowMapper<T> getRowMapper();

  public AbstractConstructedQuery() {

  }

  public AbstractConstructedQuery(DatabaseType databaseType) {
    this.databaseType = databaseType;
  }

  @Override
  public QueryBuilder newQueryBuilder() {
    return DefaultQueryBuilder.newQuery(getDatabaseType()).withFilters(columnFilterList).orderBy(orderByList);
  }

  @Override
  public boolean hasFilter(String name) {
    return ColumnFilterUtil.hasFilter(columnFilterList, name);
  }


  @Override
  public void setColumnFilterList(List<ColumnFilter> columnFilterList) {
    this.columnFilterList = columnFilterList;
  }

  @Override
  public List<ColumnFilter> getColumnFilterList() {
    if (columnFilterList == null) {
      columnFilterList = new ArrayList<>();
    }
    return columnFilterList;
  }

  @Override
  public DatabaseType getDatabaseType() {
    return databaseType;
  }

  @Override
  public List<OrderBy> getOrderByList() {
    return orderByList;
  }

  @Override
  public void setOrderByList(List<OrderBy> orderByList) {
    this.orderByList = orderByList;
  }
}
