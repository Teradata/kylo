package com.thinkbiganalytics.jobrepo.query.builder;


import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitutionFactory;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilterUtil;
import com.thinkbiganalytics.jobrepo.query.support.IdentifierUtil;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;
import com.thinkbiganalytics.jobrepo.query.support.OrderByClause;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Query Builder For usese of this see
 */
public class DefaultQueryBuilder<T> implements QueryBuilder<T> {

  private DatabaseType databaseType;
  private String from;
  private String select;
  private String where;
  private String orderBy;
  private String groupBy;
  private List<ColumnFilter> filters;
  private List<String> additionalSelectColumns;
  private List<OrderBy> orderByList;
  private List<OrderBy> defaultOrderByList;
  List<Query> joins = new LinkedList();
  private Map<String, Object> namedParameters = new HashMap<String, Object>();


  protected DefaultQueryBuilder(DatabaseType databaseType) {
    this.databaseType = databaseType;
    this.select = "";
    this.from = "";
    this.where = "";
    this.orderBy = "";
    this.groupBy = "";
    orderByList = new ArrayList<>();
    defaultOrderByList = new ArrayList<>();

  }

  public static QueryBuilder newQuery(DatabaseType databaseType) {
    return new DefaultQueryBuilder(databaseType);
  }

  @Override
  public Query build() {
    return buildWithQueryModifiers(null, null);
  }

  @Override
  public Query buildWithFilterQueryModifier(ColumnFilterQueryModifier columnFilterQueryModifier) {
    return buildWithQueryModifiers(columnFilterQueryModifier, null);
  }

  @Override
  public Query buildWithQueryModifiers(ColumnFilterQueryModifier columnFilterQueryModifier,
                                       OrderByQueryModifier orderByQueryModifier) {
    Query query = new DefaultQuery();
    //return query;
    StringBuilder sb = new StringBuilder();
    sb.append(this.select).append(" ");
    if (this.additionalSelectColumns != null && !additionalSelectColumns.isEmpty()) {
      for (String col : this.additionalSelectColumns) {
        sb.append(",");
        sb.append(col);
      }
    }
    sb.append(" ");
    sb.append(this.from).append(" ");
    if (joins != null && !joins.isEmpty()) {
      for (Query join : joins) {
        sb.append(join.getQuery());
        this.namedParameters.putAll(join.getNamedParameters());
      }
    }
    applyWhereClause(columnFilterQueryModifier);
    applyOrderBy(orderByQueryModifier);
    sb.append(this.where);
    sb.append(this.groupBy);
    String queryWithoutOrderBy = sb.toString();
    queryWithoutOrderBy = applyQuerySubstitutions(queryWithoutOrderBy);
    query.setQueryWithoutOrderBy(queryWithoutOrderBy);
    sb.append(this.orderBy);

    query.setNamedParameters(this.namedParameters);
    String finalQuery = sb.toString();
    finalQuery = applyQuerySubstitutions(finalQuery);
    query.setQuery(finalQuery);
    query.setDatabaseType(this.databaseType);
    return query;
  }

  protected void applyWhereClause(ColumnFilterQueryModifier filterQueryModifier) {
    if (filters != null) {
      where = " WHERE 1=1 ";
      int counter = 0;
      for (ColumnFilter filter : filters) {
        String bindVar = "bind_" + counter;
        bindVar += "_" + IdentifierUtil.createUniqueName(bindVar).substring(0, 2);
        applyFilterQueryConditions(filterQueryModifier, filter, bindVar);
        counter++;
      }
    }
  }

  private void applyFilterQueryConditions(ColumnFilterQueryModifier filterQueryModifier, ColumnFilter columnFilter,
                                          String bindVar) {
    if (filterQueryModifier != null) {
      filterQueryModifier.modifyFilterQueryValue(columnFilter);
    }
    if (columnFilter.isSqlString()) {
      this.where += columnFilter.getSqlString();
    } else {
      where = ColumnFilterUtil.applySqlFiltersToQuery(where, columnFilter, namedParameters, bindVar);
    }

  }

  private void applyOrderBy(OrderByQueryModifier orderByQueryModifier) {

    List<OrderBy> list = orderByList;
    if (list == null || list.isEmpty()) {
      list = defaultOrderByList;
    }
    if (list != null) {
      int count = 0;
      for (OrderBy orderBy : list) {
        if (count == 0 && !this.orderBy.toUpperCase().startsWith("ORDER BY")) {
          this.orderBy = " ORDER BY ";
        } else if (count > 0) {
          this.orderBy += ",";
        }
        if (orderByQueryModifier != null) {
          orderByQueryModifier.modifyOrderByQueryName(orderBy);
        }
        String column = orderBy.getQueryName();
        String dir = orderBy.getDir();
        String alias = orderBy.getTableAlias();

        if (StringUtils.isNotBlank(alias)) {
          alias += ".";
        }
        this.orderBy += " " + alias + " " + column + " " + dir.toUpperCase();
        count++;
      }
    }
  }

  private String applyQuerySubstitutions(String query) {
    if (this.databaseType != null) {
      return DatabaseQuerySubstitutionFactory.applyQuerySubstitutions(query, databaseType);
    }
    return query;
  }


  @Override
  public void addJoin(Query joinQuery) {
    this.joins.add(joinQuery);
  }

  @Override
  public QueryBuilder from(String from) {
    if (StringUtils.isBlank(this.from) && !from.trim().toUpperCase().startsWith("FROM")) {
      this.from = " FROM  ";
    }
    this.from += " " + from;
    return this;
  }

  @Override
  public QueryBuilder replaceFrom(String from) {
    this.from = "";
    if (StringUtils.isBlank(this.from) && !from.trim().toUpperCase().startsWith("FROM")) {
      this.from = " FROM  ";
    }
    this.from += " " + from;
    this.joins.clear();
    return this;
  }


  @Override
  public QueryBuilder select(String select) {
    if (StringUtils.isBlank(this.select) && !select.trim().toUpperCase().startsWith("SELECT")) {
      this.select = "SELECT ";
    } else if (StringUtils.isNotBlank(this.select)) {
      this.select += ", ";
    }
    this.select += " " + select;
    return this;
  }


  @Override
  public QueryBuilder addSelectColumn(String select) {
    if (this.additionalSelectColumns == null) {
      this.additionalSelectColumns = new ArrayList<>();
    }
    this.additionalSelectColumns.add(select);
    return this;
  }


  @Override
  public QueryBuilder withFilters(List<ColumnFilter> filters) {
    this.filters = filters;
    return this;
  }

  @Override
  public QueryBuilder orderBy(List<OrderBy> orderBy) {
    if (orderBy != null) {
      if (this.orderByList == null) {
        this.orderByList = new ArrayList<>();
      }
      this.orderByList.addAll(orderBy);
    }
    return this;
  }

  @Override
  public QueryBuilder withNamedParameter(String name, Object param) {
    this.namedParameters.put(name, param);
    return this;
  }


  @Override
  public QueryBuilder groupBy(String groupBy) {
    if (StringUtils.isBlank(this.groupBy) && !groupBy.trim().toUpperCase().startsWith("GROUP BY")) {
      this.groupBy = " GROUP BY ";
    } else if (StringUtils.isNotBlank(this.groupBy)) {
      this.groupBy += ",";
    }
    this.groupBy += groupBy;
    return this;
  }

  @Override
  public QueryBuilder removeDefaultOrderBy() {
    this.defaultOrderByList.clear();
    return this;
  }

  @Override
  public QueryBuilder defaultOrderBy(String column, String dir) {
    this.defaultOrderByList.add(new OrderByClause(column, dir));
    return this;
  }

  @Override
  public QueryBuilder orderBy(String column, String dir) {
    OrderBy orderBy = new OrderByClause(column, dir);
    return orderBy(orderBy);
  }

  @Override
  public QueryBuilder orderBy(OrderBy orderBy) {
    if (this.orderByList == null) {
      this.orderByList = new ArrayList<>();
    }
    this.orderByList.add(orderBy);
    return this;
  }

  @Override
  public QueryJoinBuilder innerJoin(String query) {
    QueryJoinBuilder joinBuilder = DefaultQueryJoinBuilder.innerJoin(this, new DefaultQuery(query));
    return joinBuilder;
  }

  @Override
  public QueryJoinBuilder leftJoin(String query) {
    QueryJoinBuilder joinBuilder = DefaultQueryJoinBuilder.leftJoin(this, new DefaultQuery(query));
    return joinBuilder;
  }

  @Override
  public QueryJoinBuilder innerJoin(Query query) {
    QueryJoinBuilder joinBuilder = DefaultQueryJoinBuilder.innerJoin(this, query);
    return joinBuilder;
  }

  @Override
  public QueryJoinBuilder leftJoin(Query query) {
    QueryJoinBuilder joinBuilder = DefaultQueryJoinBuilder.leftJoin(this, query);
    return joinBuilder;
  }


}
