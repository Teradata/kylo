package com.thinkbiganalytics.jobrepo.query;

import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;

import java.util.List;

/**
 * Created by sr186054 on 4/14/16.
 */
public interface ConstructedQuery<T> {

  Query buildQuery();

  QueryBuilder newQueryBuilder();

  QueryBuilder getQueryBuilder();

  boolean hasFilter(String name);

  void setColumnFilterList(List<ColumnFilter> columnFilterList);

  List<ColumnFilter> getColumnFilterList();

  DatabaseType getDatabaseType();

  List<OrderBy> getOrderByList();

  void setOrderByList(List<OrderBy> orderByList);
}
