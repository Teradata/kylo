package com.thinkbiganalytics.jobrepo.rest.support;


import com.thinkbiganalytics.jobrepo.query.support.OrderBy;
import com.thinkbiganalytics.jobrepo.query.support.OrderByClause;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 8/14/15.
 */
public class RestUtil {

  public static List<OrderBy> buildOrderByList(String sort, DataTableColumnFactory.PIPELINE_DATA_TYPE dataType) {
    List<OrderBy> orderByList = new ArrayList<>();
    if (StringUtils.isNotBlank(sort)) {
      String[] orderBy = sort.split(",");

      for (String orderByField : orderBy) {
        String direction = "asc";
        String field = orderByField;
        if (orderByField.startsWith("-")) {
          direction = "desc";
          field = orderByField.substring(1);
        }
        if (dataType != null) {
          field = DataTableColumnFactory.getDataTableColumn(dataType, field);
        }

        orderByList.add(new OrderByClause(field, direction));
      }
    }
    return orderByList;
  }

}
