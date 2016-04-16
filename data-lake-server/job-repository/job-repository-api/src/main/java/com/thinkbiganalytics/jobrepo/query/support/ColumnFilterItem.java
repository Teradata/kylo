package com.thinkbiganalytics.jobrepo.query.support;


import java.util.Collection;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface ColumnFilterItem {

  String INTERNAL_QUERY_FLAG_PREFIX = "_";

  void setApplyToWhereClause(boolean applyToWhereClause);

  String getSqlConditionBeforeOperator();

  void setSqlConditionBeforeOperator(String sqlConditionBeforeOperator);

  String getName();

  void setName(String name);

  Object getValue();

  void setValue(Object value);

  String getOperator();

  void setOperator(String operator);

  Object getSqlValue();

  String getStringValue();

  Collection<?> getValueAsCollection();

  boolean isCollection();

  String getDataType();

  void setDataType(String dataType);

  boolean isApplyToWhereClause();

  String getQueryName();

  void setQueryName(String queryName);

  String getTableAlias();

  void setTableAlias(String tableAlias);
}
