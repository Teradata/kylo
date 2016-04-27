package com.thinkbiganalytics.jobrepo.query.rowmapper;


import com.thinkbiganalytics.jobrepo.query.model.TbaJobExecution;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 8/13/15.
 */
public class JobExecutionRowMapper implements RowMapper<TbaJobExecution> {

  private List<String> columnNames = new ArrayList<String>();

  public JobExecutionRowMapper() {
  }

  Map<Long, JobInstance> jobInstanceMap = new HashMap<Long, JobInstance>();
  Map<Long, TbaJobExecution> jobExecutionMap = new HashMap<Long, TbaJobExecution>();

  private void fetchColumnMetaData(ResultSet rs) throws SQLException {
    ResultSetMetaData rsmd = rs.getMetaData();
    int i = 0;
    while (i < rsmd.getColumnCount()) {
      i++;
      String columnName = rsmd.getColumnName(i);
      int columnType = rsmd.getColumnType(i);
      String tableName = rsmd.getTableName(i);
      columnNames.add(columnName);
    }
  }

  protected JobParameters fetchJobParameters(ResultSet rs) {
    return null;
  }

  private void fetchBatchExecutionContextValues(JobExecution je, ResultSet rs) throws SQLException {

  }

  public TbaJobExecution mapRow(ResultSet rs, int rowNum) throws SQLException {

    // ji.JOB_INSTANCE_ID, ji.JOB_NAME, ji.JOB_KEY, e.JOB_EXECUTION_ID, e.START_TIME, e.END_TIME, e.STATUS, e.EXIT_CODE, e.EXIT_MESSAGE, e.CREATE_TIME, e.LAST_UPDATED, e.VERSION, e.JOB_CONFIGURATION_LOCATION, p2.STRING_VAL as FEED_NAME

    if (columnNames.isEmpty()) {
      fetchColumnMetaData(rs);
    }

    Long jobInstanceId = Long.valueOf(rs.getLong("JOB_INSTANCE_ID"));
    Long jobExecutionId = Long.valueOf(rs.getLong("JOB_EXECUTION_ID"));
    String jobName = rs.getString("JOB_NAME");
    String jobKey = rs.getString("JOB_KEY");
    String jobType = rs.getString("JOB_TYPE");
    Timestamp startTime = rs.getTimestamp("START_TIME");
    Timestamp endTime = rs.getTimestamp("END_TIME");
    String status = rs.getString("STATUS");
    String exitCode = rs.getString("EXIT_CODE");
    String exitMessage = rs.getString("EXIT_MESSAGE");
    Timestamp createTime = rs.getTimestamp("CREATE_TIME");
    Timestamp lastUpdated = rs.getTimestamp("LAST_UPDATED");
    Integer version = Integer.valueOf(rs.getInt("VERSION"));
    String jobConfigurationLocation = rs.getString("JOB_CONFIGURATION_LOCATION");
    Long runTime = null;
    Long timeSinceEndTime = null;
    if (columnNames.contains("RUN_TIME")) {
      runTime = rs.getLong("RUN_TIME") * 1000;
    }
    if (columnNames.contains("TIME_SINCE_END_TIME")) {
      timeSinceEndTime = rs.getLong("TIME_SINCE_END_TIME") * 1000;
    }

    boolean isLatestJobExecution = false;
    if (columnNames.contains("IS_LATEST")) {
      String isLatest = rs.getString("IS_LATEST");
      if (StringUtils.isNotBlank(isLatest)) {
        isLatestJobExecution = BooleanUtils.toBoolean(isLatest);

      }
    }
    String feedName = null;
    if (columnNames.contains("FEED_NAME")) {
      feedName = rs.getString("FEED_NAME");
    }

    if (!jobInstanceMap.containsKey(jobInstanceId)) {
      jobInstanceMap.put(jobInstanceId, new JobInstance(jobInstanceId, jobName));
    }
    JobInstance jobInstance = jobInstanceMap.get(jobInstanceId);
    JobParameters jobParameters = fetchJobParameters(rs);

    TbaJobExecution jobExecution = null;
    if (!jobExecutionMap.containsKey(jobExecutionId)) {
      jobExecution = new TbaJobExecution(jobInstance, jobExecutionId, jobParameters, jobConfigurationLocation);
      jobExecution.setRunTime(runTime);
      jobExecution.setTimeSinceEndTime(timeSinceEndTime);
      jobExecution.setStartTime(startTime);
      jobExecution.setEndTime(endTime);
      jobExecution.setStatus(BatchStatus.valueOf(status));
      jobExecution.setExitStatus(new ExitStatus(exitCode, exitMessage));
      jobExecution.setCreateTime(createTime);
      jobExecution.setLastUpdated(lastUpdated);
      jobExecution.setVersion(version);
      jobExecution.setJobType(jobType);
      jobExecution.setIsLatest(isLatestJobExecution);
      jobExecution.setFeedName(feedName);
      jobExecutionMap.put(jobExecutionId, jobExecution);
    }
    jobExecution = jobExecutionMap.get(jobExecutionId);
    //add in additional batch context values if found

    return jobExecution;
  }
}
