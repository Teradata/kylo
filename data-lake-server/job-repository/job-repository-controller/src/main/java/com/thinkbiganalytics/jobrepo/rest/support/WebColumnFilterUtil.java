package com.thinkbiganalytics.jobrepo.rest.support;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilterUtil;
import com.thinkbiganalytics.jobrepo.query.support.QueryColumnFilterSqlString;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by sr186054 on 8/14/15.
 */
public class WebColumnFilterUtil extends ColumnFilterUtil {


  public static List<ColumnFilter> buildFiltersFromRequest(HttpServletRequest request) {
    return buildFiltersFromRequestForDatatable(request, (DataTableColumnFactory.PIPELINE_DATA_TYPE) null);
  }


  public static List<ColumnFilter> buildFiltersFromRequestForDatatable(HttpServletRequest request,
                                                                       DataTableColumnFactory.PIPELINE_DATA_TYPE dataType) {
    Map<String, String[]> parameters = request.getParameterMap();
    List<ColumnFilter> inlineFilter = new ArrayList<ColumnFilter>();
    String prefix = "param";
    String operatorSeparator = "::";
    for (String key : parameters.keySet()) {
      if (key.startsWith(prefix) || ColumnFilterUtil.nonFieldRequestParameters.indexOf(key) == -1) {
        String operator = "=";
        String field = key.startsWith(prefix) ? StringUtils.substringAfter(key, prefix) : key;
        if (field.endsWith(">")) {
          field = field.substring(0, field.length() - 1);
          operator = ">=";
        } else if (field.endsWith("<")) {
          field = field.substring(0, field.length() - 1);
          operator = "<=";
        }
        String[] vals = parameters.get(key);
        for (String val : vals) {

          if (val.contains(operatorSeparator)) {
            operator = StringUtils.substringAfter(val, operatorSeparator);
            val = StringUtils.substringBefore(val, operatorSeparator);
          } else if (val.contains(",")) {
            //make it a IN clause
            operator = "IN";
            if (val.endsWith(",")) {
              val = val.substring(0, val.length() - 1);
            }
          }

          if (dataType != null) {
            field = DataTableColumnFactory.getDataTableColumn(dataType, field);
          }
          inlineFilter.add(new QueryColumnFilterSqlString(field, val, operator));
        }
      }
    }
    return inlineFilter;
  }


  public static List<ColumnFilter> buildFiltersFromRequest(HttpServletRequest request, String jsonFilter) {
    List<ColumnFilter> filters = null;
    if (StringUtils.isNotBlank(jsonFilter)) {
      ObjectMapper mapper = new ObjectMapper();
      try {
        filters = Arrays.asList(mapper.readValue(jsonFilter, ColumnFilter[].class));
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      filters = WebColumnFilterUtil.buildFiltersFromRequest(request);
    }
    return filters;
  }


}
