package com.thinkbiganalytics.jobrepo.query.support;


import com.thinkbiganalytics.jobrepo.query.model.ExecutionStatus;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 9/2/15.
 */
public class DaoUtil {


  public static String errorJobStatusSqlString() {
    return "'FAILED','UNKNOWN'";
  }


  public static String errorExitCodeSqlString() {
    return "'FAILED'";
  }

  public static String getHealthStateSqlClause(String batchJobExectionAlias) {
    String alias = batchJobExectionAlias;
    return "CASE WHEN " + alias + ".status IN('FAILED','UNKNOWN') OR " + alias + ".EXIT_CODE IN('FAILED') then 'FAILED' " +
           "WHEN " + alias + ".status in ('STARTED','STARTING') THEN 'RUNNING' " +
           "else " + alias + ".status " +
           "END ";
  }

/*
    public static Map<BatchStatus,Long> convertJobStatusCountResult( List<JobStatusCount> jobStatusCountList) {
        Map<BatchStatus, Long> result = new HashMap<BatchStatus, Long>();
        if(jobStatusCountList != null && !jobStatusCountList.isEmpty()){
            for(JobStatusCount jobStatusCount : jobStatusCountList){
                String status = jobStatusCount.getStatus();
                Long count = jobStatusCount.getCount();
                result.put(BatchStatus.valueOf(status), count);
            }
        }
        return result;

    }
    */

  public static Map<ExecutionStatus, Long> convertJobExecutionStatusCountResult(List<JobStatusCount> jobStatusCountList) {
    Map<ExecutionStatus, Long> result = new HashMap<ExecutionStatus, Long>();
    if (jobStatusCountList != null && !jobStatusCountList.isEmpty()) {
      for (JobStatusCount jobStatusCount : jobStatusCountList) {
        String status = jobStatusCount.getStatus();
        Long count = jobStatusCount.getCount();
        result.put(ExecutionStatus.valueOf(status), count);
      }
    }
    return result;

  }

}
