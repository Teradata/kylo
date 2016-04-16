package com.thinkbiganalytics.jobrepo.query.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 8/12/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderByClause implements OrderBy {

  private String columnName;
  private String dir;
  private String queryName;
  private String tableAlias;

  public OrderByClause() {

  }

  public OrderByClause(@JsonProperty("columnName") String columnName, @JsonProperty("dir") String dir) {
    this.columnName = columnName;
    this.dir = dir;

  }


  @Override
  public String getColumnName() {
    return columnName;
  }

  @Override
  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  @Override
  public String getDir() {
    return dir;
  }

  @Override
  public void setDir(String dir) {
    this.dir = dir;
  }

  public static List<OrderBy> convert(String orderBy) {
    return convert(orderBy, null);
  }

  /**
   * FIELD asc, FIELD2 desc, FIELD3 asc
   */
  public static List<OrderBy> convert(String orderBy, Map<String, String> columnMap) {
    List<OrderBy> list = null;
    if (StringUtils.isNotBlank(orderBy)) {
      list = new ArrayList<OrderBy>();
      String[] order = orderBy.split(",");
      for (String item : order) {
        String field = StringUtils.substringBefore(item, " ");
        if (columnMap != null && StringUtils.isNotBlank(field)) {
          String dbColumn = columnMap.get(field.trim().toLowerCase());
          if (StringUtils.isNotBlank(dbColumn)) {
            field = dbColumn;
          }
        }
        String dir = StringUtils.substringAfterLast(item, " ");
        if (dir == null) {
          dir = "asc";
        }
        OrderBy ob = new OrderByClause(field.trim(), dir.trim());
        list.add(ob);
      }
    }
    return list;
  }


  @Override
  public String getQueryName() {
    if (StringUtils.isNotBlank(queryName)) {
      return queryName;
    }
    return columnName;
  }

  @Override
  public void setQueryName(String queryName) {
    this.queryName = queryName;
  }

  @Override
  public String getTableAlias() {
    if (tableAlias == null) {
      tableAlias = "";
    }
    return tableAlias;
  }

  @Override
  public void setTableAlias(String tableAlias) {
    this.tableAlias = tableAlias;
  }
}
