package com.thinkbiganalytics.jobrepo.query.builder;

import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;

import java.util.List;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface QueryBuilder<T> {

  Query build();

  Query buildWithFilterQueryModifier(ColumnFilterQueryModifier columnFilterQueryModifier);

  Query buildWithQueryModifiers(ColumnFilterQueryModifier columnFilterQueryModifier, OrderByQueryModifier orderByQueryModifier);

  void addJoin(Query joinQuery);

  QueryBuilder from(String from);

  QueryBuilder replaceFrom(String from);

  QueryBuilder select(String select);

  QueryBuilder addSelectColumn(String select);

  QueryBuilder withFilters(List<ColumnFilter> filters);

  QueryBuilder orderBy(List<OrderBy> orderBy);

  QueryBuilder withNamedParameter(String name, Object param);

  QueryBuilder groupBy(String groupBy);

  QueryBuilder removeDefaultOrderBy();

  QueryBuilder defaultOrderBy(String column, String dir);

  QueryBuilder orderBy(String column, String dir);

  QueryBuilder orderBy(OrderBy orderBy);

  QueryJoinBuilder innerJoin(String query);

  QueryJoinBuilder leftJoin(String query);

  QueryJoinBuilder innerJoin(Query query);

  QueryJoinBuilder leftJoin(Query query);
}
