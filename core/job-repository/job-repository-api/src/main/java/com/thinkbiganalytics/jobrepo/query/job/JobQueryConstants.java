package com.thinkbiganalytics.jobrepo.query.job;

import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;

import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by sr186054 on 9/25/15.
 */
public interface JobQueryConstants {

  String QUERY_RUN_TIME = "RUN_TIME";
  String QUERY_JOB_TYPE = "JOB_TYPE";
  String QUERY_LOOKBACK_TIME = "LOOKBACK_TIME";
  String USE_ACTIVE_JOBS_QUERY = ColumnFilter.INTERNAL_QUERY_FLAG_PREFIX + "ACTIVE_JOBS_QUERY";
  String QUERY_LATEST_JOB = "IS_LATEST";
  String DAY_DIFF_FROM_NOW = "DAY_DIFF_FROM_NOW";
  String WEEK_DIFF_FROM_NOW = "WEEK_DIFF_FROM_NOW";
  String MONTH_DIFF_FROM_NOW = "MONTH_DIFF_FROM_NOW";
  String YEAR_DIFF_FROM_NOW = "YEAR_DIFF_FROM_NOW";


  List<String> jobInstanceColumnNames = CollectionUtils.arrayToList("JOB_INSTANCE_ID,JOB_NAME,JOB_KEY".split(","));


}

