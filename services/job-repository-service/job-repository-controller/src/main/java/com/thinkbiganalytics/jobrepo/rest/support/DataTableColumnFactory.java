package com.thinkbiganalytics.jobrepo.rest.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.jobrepo.query.job.JobQueryConstants;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;
import com.thinkbiganalytics.jobrepo.query.support.OrderByClause;
import com.thinkbiganalytics.jobrepo.rest.model.datatables.DataTablesQueryParams;
import com.thinkbiganalytics.jobrepo.rest.model.datatables.VisualSearchFilter;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 8/14/15.
 */
public class DataTableColumnFactory {

  public static enum PIPELINE_DATA_TYPE {
    FEED, JOB
  }

  /**
   * adds the uiColummn as a key as well as a column without whitespace as a key to the map returns the dbColumn for the specified
   * UIColumn
   */
  private static void addToColumnMap(Map<String, String> map, String uiColumn, String dbColumn) {
    if (StringUtils.isNotBlank(uiColumn) && StringUtils.isNotBlank(dbColumn)) {
      String key = uiColumn.toLowerCase();
      String noWhiteSpaceKey = StringUtils.deleteWhitespace(key);
      map.put(key, dbColumn);
      if (!key.equalsIgnoreCase(noWhiteSpaceKey)) {
        map.put(noWhiteSpaceKey, dbColumn);
      }
    }
  }

  private static Map<String, String> getFeedDataTableColumnMap() {
    Map<String, String> map = getJobDataTableColumnMap();
    addToColumnMap(map, "name", "FEED_NAME");
    addToColumnMap(map, "feed instance id", "JOB_INSTANCE_ID");
    addToColumnMap(map, "feed execution id", "JOB_EXECUTION_ID");
    addToColumnMap(map, "latest feed", "IS_LATEST");
    return map;
  }

  private static Map<String, String> getJobDataTableColumnMap() {
    Map<String, String> map = new HashMap<String, String>();
    addToColumnMap(map, "name", "JOB_NAME");
    addToColumnMap(map, "feed", "FEED_NAME");
    addToColumnMap(map, "feed name", "FEED_NAME");
    addToColumnMap(map, "job name", "JOB_NAME");
    addToColumnMap(map, "started", "START_TIME");
    addToColumnMap(map, "start time", "START_TIME");
    addToColumnMap(map, "job id", "JOB_EXECUTION_ID");
    addToColumnMap(map, "instance id", "JOB_INSTANCE_ID");
    addToColumnMap(map, "execution id", "JOB_EXECUTION_ID");
    addToColumnMap(map, "exit code", "EXIT_CODE");
    addToColumnMap(map, "status", "STATUS");
    addToColumnMap(map, "run time", "RUN_TIME");
    addToColumnMap(map, "job type", "JOB_TYPE");
    addToColumnMap(map, "is valid", "IS_VALID");
    addToColumnMap(map, "validation message", "VALIDATION_MESSAGE");
    addToColumnMap(map, "is latest", "IS_LATEST");
    addToColumnMap(map, "latest job", "IS_LATEST");
    addToColumnMap(map, "latest", "IS_LATEST");
    map.put(JobQueryConstants.USE_ACTIVE_JOBS_QUERY.toLowerCase(), JobQueryConstants.USE_ACTIVE_JOBS_QUERY);

    return map;
  }

  public static String getJobQueryColumn(String column) {
    return getJobDataTableColumnMap().get(column.toLowerCase());
  }

  public static String getFeedQueryColumn(String dataTableColumn) {
    return getFeedDataTableColumnMap().get(dataTableColumn.toLowerCase());
  }

  public static Map<String, String> getDatabaseColumnMap(PIPELINE_DATA_TYPE type) {
    if (PIPELINE_DATA_TYPE.FEED.equals(type)) {
      return DataTableColumnFactory.getFeedDataTableColumnMap();
    } else if (PIPELINE_DATA_TYPE.JOB.equals(type)) {
      return DataTableColumnFactory.getJobDataTableColumnMap();
    } else {
      return new HashMap<String, String>();
    }
  }


  public static String getDataTableColumn(PIPELINE_DATA_TYPE type, String dataTableColumn) {
    if (PIPELINE_DATA_TYPE.FEED.equals(type)) {
      return DataTableColumnFactory.getFeedQueryColumn(dataTableColumn);
    } else if (PIPELINE_DATA_TYPE.JOB.equals(type)) {
      return DataTableColumnFactory.getJobQueryColumn(dataTableColumn);
    }
    return "";
  }


  public static Map<String, String> getPipelineQueryParams(PIPELINE_DATA_TYPE type, DataTablesQueryParams params) {
    //convert this into the JSON string pipeline is expecting
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> searchNameValueMap = params.getFilterMap();

    for (Map.Entry<String, String> entry : searchNameValueMap.entrySet()) {
      String pipelineColumn = DataTableColumnFactory.getDataTableColumn(type, entry.getKey());
      if (StringUtils.isNotBlank(pipelineColumn)) {
        queryParams.put("param" + pipelineColumn, entry.getValue());
      }
    }
    return queryParams;
  }

  public static Map<String, String> getPipelineQueryParams(PIPELINE_DATA_TYPE type, List<VisualSearchFilter> filters) {
    //convert this into the JSON string pipeline is expecting
    Map<String, String> queryParams = new HashMap<String, String>();
    for (VisualSearchFilter filter : filters) {
      String pipelineColumn = DataTableColumnFactory.getDataTableColumn(type, filter.getKey());
      if (StringUtils.isNotBlank(pipelineColumn)) {
        queryParams.put("param" + pipelineColumn, filter.getValue() + "::" + filter.getOperator());
      }
    }
    return queryParams;
  }

  public static List<OrderBy> getPipelineOrderByQueryParam(PIPELINE_DATA_TYPE type, String dataTablesJson) {
    DataTablesQueryParams dataTablesQueryParams = DataTableColumnFactory.parseParameters(dataTablesJson);
    return DataTableColumnFactory.getPipelineOrderByQueryParam(type, dataTablesQueryParams);
  }

  public static List<OrderBy> getPipelineOrderByQueryParam(PIPELINE_DATA_TYPE type, DataTablesQueryParams params) {
    List<OrderBy> orderBy = new ArrayList<OrderBy>();
    if (params != null) {
      Map<String, String> orderMap = params.getOrderMap();
      for (Map.Entry<String, String> entry : orderMap.entrySet()) {
        String pipelineColumn = DataTableColumnFactory.getDataTableColumn(type, entry.getKey());
        if (StringUtils.isNotBlank(pipelineColumn)) {
          orderBy.add(new OrderByClause(pipelineColumn, entry.getValue()));
        }
      }
    }
    return orderBy;
  }

  public static DataTablesQueryParams parseParameters(String params) {
    ObjectMapper mapper = new ObjectMapper();
    DataTablesQueryParams dataTablesQueryParams = null;
    if (StringUtils.isNotBlank(params)) {
      try {
        dataTablesQueryParams = mapper.readValue(params, DataTablesQueryParams.class);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return dataTablesQueryParams;
  }

  public static List<OrderBy> parseOrderBy(PIPELINE_DATA_TYPE type, String orderByString) {
    return OrderByClause.convert(orderByString, DataTableColumnFactory.getDatabaseColumnMap(type));
  }
}
