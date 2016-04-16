package com.thinkbiganalytics.jobrepo.query.support;

import com.thinkbiganalytics.jobrepo.query.job.JobQueryConstants;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitution;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitutionFactory;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 8/14/15.
 */
public class ColumnFilterUtil {

  public static List<String> nonFieldRequestParameters = new ArrayList<>();


  static {
    nonFieldRequestParameters.add("q");
    nonFieldRequestParameters.add("limit");
    nonFieldRequestParameters.add("sort");
    nonFieldRequestParameters.add("start");
  }

  public static String getSqlBindParamString(String operator, String bindParam) {

    if (operator.equalsIgnoreCase("in") || operator.equalsIgnoreCase("not in")) {
      return operator + " (:" + bindParam + ") ";
    } else {
      return operator + " :" + bindParam;
    }

  }


  /**
   * @param query
   * @param filter
   * @param namedParameters
   * @param bindParam
   * @return
   */
  public static String applySqlFiltersToQuery(String query, ColumnFilter filter, Map<String, Object> namedParameters,
                                              String bindParam) {
    int counter = 0;
    String bindVar = bindParam + "_" + counter;
    List<ColumnFilterItem> filtersToApply = new ArrayList<ColumnFilterItem>();
    if (filter.getFilters() != null && !filter.getFilters().isEmpty()) {
      filtersToApply.addAll(filter.getFilters());
    } else {
      filtersToApply.add(filter);
    }
    for (ColumnFilterItem aFilter : filtersToApply) {
      if (aFilter.isApplyToWhereClause()) {
        bindVar = bindParam + "_" + counter;
        String tableAlias = aFilter.getTableAlias();
        if (StringUtils.isNotBlank(tableAlias)) {
          tableAlias += ".";
        }
        query +=
            " AND " + tableAlias + aFilter.getSqlConditionBeforeOperator() + " " + ColumnFilterUtil
                .getSqlBindParamString(aFilter.getOperator(), bindVar) + " ";
        namedParameters.put(bindVar, aFilter.getSqlValue());
        counter++;
      }
    }
    return query;
  }


  public static boolean hasFilter(List<ColumnFilter> filters, String name) {
    if (filters != null && !filters.isEmpty()) {
      for (ColumnFilter f : filters) {
        if (name.equalsIgnoreCase(f.getName())) {
          return true;
        }
      }
    }
    return false;
  }


  public static boolean applyDatabaseTypeDateDiffSql(DatabaseType databaseType, ColumnFilter columnFilter) {
    String name = columnFilter.getNameOrFirstFilterName();

    DatabaseQuerySubstitution.DATE_PART datePart = null;
    if (JobQueryConstants.DAY_DIFF_FROM_NOW.equals(name)) {
      datePart = DatabaseQuerySubstitution.DATE_PART.DAY;
    } else if (JobQueryConstants.WEEK_DIFF_FROM_NOW.equals(name)) {
      datePart = DatabaseQuerySubstitution.DATE_PART.WEEK;
    } else if (JobQueryConstants.MONTH_DIFF_FROM_NOW.equals(name)) {
      datePart = DatabaseQuerySubstitution.DATE_PART.MONTH;
    } else if (JobQueryConstants.YEAR_DIFF_FROM_NOW.equals(name)) {
      datePart = DatabaseQuerySubstitution.DATE_PART.YEAR;
    }
    if (datePart != null) {
      Object value = columnFilter.getValue();
      Integer filterValue = 1;
      if (value != null) {
        try {
          filterValue = (Integer) value;
        } catch (NumberFormatException | ClassCastException e) {

        }
      }
      if (filterValue == null) {
        filterValue = 1;
      }

      columnFilter.setTableAlias("");
      columnFilter.setValue(filterValue);
      columnFilter.setSqlString(" AND " + DatabaseQuerySubstitutionFactory.getDatabaseSubstitution(databaseType)
          .getDateDiffWhereClause("e.START_TIME", datePart, filterValue) + " ");
      return true;
    }
    return false;
  }


}
