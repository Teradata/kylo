package com.thinkbiganalytics.jobrepo.repository.dao;

import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.jobrepo.repository.TimeZoneUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;

/**
 * Responsible for taking a ExecutionContext (either Step or Job) and persisting the values in the Context map to the custom
 * BATCH_EXECUTION_CONTEXT_VALUES table <p> Created by sr186054 on 8/27/15
 */
@Named
public class ExecutionContextValuesDao implements InitializingBean {

  private static Logger LOG = LoggerFactory.getLogger(ExecutionContextValuesDao.class);

  private static final String TABLE_NAME = "BATCH_EXECUTION_CONTEXT_VALUES";

  public static enum ExecutionContextParameterType {
    STRING,
    DATE,
    LONG,
    DOUBLE,
    BOOLEAN;

    private ExecutionContextParameterType() {
    }
  }


  public static enum ExecutionContextType {
    JOB,
    STEP;

    private ExecutionContextType() {
    }
  }

  public ExecutionContextValuesDao() {

  }


  @Autowired
  private JdbcTemplate jdbcTemplate;

  public JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(this.jdbcTemplate);
  }

  /**
   * Save all parameters in the StepExecution.ExecutionContext to the table
   */
  public void saveStepExecutionContextValues(StepExecution stepExecution) {
    saveStepExecutionContextValues(stepExecution, null);
  }

  /**
   * Save specific parameters matching the keys passed in via the List
   */
  public void saveStepExecutionContextValues(StepExecution stepExecution, List<String> stepExecutionKeys) {
    Long jobExecutionId = stepExecution.getJobExecutionId();
    saveExecutionContextValues(jobExecutionId, stepExecution.getId(), ExecutionContextType.STEP,
                               stepExecution.getExecutionContext(), stepExecutionKeys);
  }


  /**
   * Save all the parameters in the JobExecution.ExecutionContext to the table
   */
  public void saveJobExecutionContextValues(JobExecution jobExecution) {
    saveJobExecutionContextValues(jobExecution, null);
  }

  /**
   * Save specific parameters matching the keys passed in via the List for the JobExecution.ExecutionContext
   */
  public void saveJobExecutionContextValues(JobExecution jobExecution, List<String> jobExecutionKeys) {
    Long jobExecutionId = jobExecution.getId();
    saveExecutionContextValues(jobExecutionId, null, ExecutionContextType.JOB, jobExecution.getExecutionContext(),
                               jobExecutionKeys);
  }


  public void mergeStepExecutionContextValues(StepExecution stepExecution) {
    mergeStepExecutionContextValues(stepExecution, null);
  }

  /**
   * Save specific parameters matching the keys passed in via the List
   */
  public void mergeStepExecutionContextValues(StepExecution stepExecution, List<String> stepExecutionKeys) {
    Long jobExecutionId = stepExecution.getJobExecutionId();
    mergeExecutionContextValues(jobExecutionId, stepExecution.getId(), ExecutionContextType.STEP,
                                stepExecution.getExecutionContext(), stepExecutionKeys);
  }


  public void mergeJobExecutionContextValues(JobExecution jobExecution) {
    mergeJobExecutionContextValues(jobExecution, null);
  }

  public void mergeJobExecutionContextValues(JobExecution jobExecution, List<String> jobExecutionKeys) {
    Long jobExecutionId = jobExecution.getId();
    mergeExecutionContextValues(jobExecutionId, null, ExecutionContextType.JOB, jobExecution.getExecutionContext(),
                                jobExecutionKeys);
  }

  private void saveExecutionContextValues(Long jobExecutionId, Long stepExecutionId, ExecutionContextType executionContextType,
                                          ExecutionContext executionContext, List<String> stepExecutionKeys) {

    for (Map.Entry<String, Object> params : executionContext.entrySet()) {
      String key = params.getKey();
      Object value = params.getValue();
      if (stepExecutionKeys == null || (stepExecutionKeys != null && stepExecutionKeys.contains(key))) {
        insertParameter(jobExecutionId, stepExecutionId, executionContextType, key, value);
      }
    }
  }


  private void mergeExecutionContextValues(Long jobExecutionId, Long stepExecutionId, ExecutionContextType executionContextType,
                                           ExecutionContext executionContext, List<String> stepExecutionKeys) {

    //1 first query to see if this step has saved execution context
    Set<String> keysToUpdate = new HashSet<>();
    Map<String, Object> saved = getExecutionContextValues(jobExecutionId, stepExecutionId);
    if (saved != null && !saved.isEmpty()) {
      keysToUpdate.addAll(saved.keySet());
    }

    for (Map.Entry<String, Object> params : executionContext.entrySet()) {
      String key = params.getKey();
      Object value = params.getValue();
      if (stepExecutionKeys == null || (stepExecutionKeys != null && stepExecutionKeys.contains(key))) {
        if (keysToUpdate.contains(key)) {
          updateParameter(jobExecutionId, stepExecutionId, executionContextType, key, value);
        } else {
          insertParameter(jobExecutionId, stepExecutionId, executionContextType, key, value);
        }
      }
    }
  }

  public List<String> getExecutionContextKeys(Long jobExecutionId, Long stepExecutionId) {
    String query = "SELECT DISTINCT KEY_NAME FROM " + TABLE_NAME + " WHERE JOB_EXECUTION_ID = ? and STEP_EXECUTION_ID = ?";
    Object[] args = new Object[]{jobExecutionId, stepExecutionId};
    return getJdbcTemplate().query(query, args, new RowMapper<String>() {
      @Override
      public String mapRow(ResultSet resultSet, int i) throws SQLException {
        String key = resultSet.getString("KEY_NAME");
        return key;
      }
    });
  }

  public List<String> getExecutionContextKeysForFeed(String feedName) {
    String query = "SELECT DISTINCT t.KEY_NAME FROM " + TABLE_NAME + " t "
                   + "INNER JOIN BATCH_JOB_EXECUTION_PARAMS p on p.JOB_EXECUTION_ID = t.JOB_EXECUTION_ID "
                   + "WHERE p.KEY_NAME = ? AND p.STRING_VAL = ? ";
    Object[] args = new Object[]{FeedConstants.PARAM__FEED_NAME, feedName};
    return getJdbcTemplate().query(query, args, new RowMapper<String>() {
      @Override
      public String mapRow(ResultSet resultSet, int i) throws SQLException {
        String key = resultSet.getString("KEY_NAME");
        return key;
      }
    });
  }

  public Map<String, Object> getExecutionContextValues(Long jobExecutionId, Long stepExecutionId) {
    String query = "SELECT * FROM " + TABLE_NAME + " WHERE JOB_EXECUTION_ID = ? and STEP_EXECUTION_ID = ?";

    Object[] args = new Object[]{jobExecutionId, stepExecutionId};
    Map<String, Object> data = new HashMap<>();
    List<Map<String, Object>> results = this.getJdbcTemplate().query(query, args, new RowMapper<Map<String, Object>>() {
      @Override
      public Map<String, Object> mapRow(ResultSet rs, int i) throws SQLException {
        String typeCode = rs.getString("TYPE_CD");

        String valCol = typeCode + "_VAL";
        Object val = rs.getObject(valCol);
        String key = rs.getString("KEY_NAME");
        Map<String, Object> m = new HashMap<String, Object>();
        m.put(key, val);
        return m;
      }
    });
    //merge the list back into a map
    if (results != null && !results.isEmpty()) {
      for (Map<String, Object> row : results) {
        data.putAll(row);
      }
    }
    return data;

  }



  private void insertParameter(Long executionId, Long stepExecutionId, ExecutionContextType executionContextType, String key,
                               Object value) {
    Long longValue = Long.valueOf(0L);
    Double doubleValue = Double.valueOf(0.0D);
    String stringValue = value.toString();
    Date dateValue = null;
    ExecutionContextParameterType type = ExecutionContextParameterType.STRING;

    if (value instanceof Boolean) {
      type = ExecutionContextParameterType.BOOLEAN;
    } else if (value instanceof Long) {
      longValue = (Long) value;
      type = ExecutionContextParameterType.LONG;
    } else if (value instanceof Double) {
      doubleValue = (Double) value;
      type = ExecutionContextParameterType.DOUBLE;
    } else if (value instanceof Date) {
      dateValue = (Date) value;
      type = ExecutionContextParameterType.DATE;
    }

    //  int[] argTypes = new int[]{-5, -5, 12, 12, 12, 12, 93, -5, 8, 93};
    int[] argTypes = new int[]{Types.BIGINT, Types.BIGINT, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP, Types.BIGINT, Types.DOUBLE, Types.TIMESTAMP};
    Object[]
        args =
        new Object[]{executionId, stepExecutionId, executionContextType.name(), key, type.name(), stringValue, dateValue,
                     longValue, doubleValue, TimeZoneUtil.getUTCTime()};
    this.getJdbcTemplate().update("INSERT into " + TABLE_NAME
                                  + "(JOB_EXECUTION_ID,STEP_EXECUTION_ID,EXECUTION_CONTEXT_TYPE,KEY_NAME, TYPE_CD, STRING_VAL, DATE_VAL, LONG_VAL, DOUBLE_VAL, CREATE_DATE) values (?,?,?, ?, ?, ?, ?, ?, ?,?)",
                                  args, argTypes);
  }

  private void updateParameter(Long executionId, Long stepExecutionId, ExecutionContextType executionContextType, String key,
                               Object value) {
    Long longValue = Long.valueOf(0L);
    Double doubleValue = Double.valueOf(0.0D);
    String stringValue = value.toString();
    Date dateValue = null;
    ExecutionContextParameterType type = ExecutionContextParameterType.STRING;

    if (value instanceof Boolean) {
      type = ExecutionContextParameterType.BOOLEAN;
    } else if (value instanceof Long) {
      longValue = (Long) value;
      type = ExecutionContextParameterType.LONG;
    } else if (value instanceof Double) {
      doubleValue = (Double) value;
      type = ExecutionContextParameterType.DOUBLE;
    } else if (value instanceof Date) {
      dateValue = (Date) value;
      type = ExecutionContextParameterType.DATE;
    }

    //int[] argTypes = new int[]{-5, -5, 12, 12, 12, 12, 93, -5, 8, 93};
    int[] argTypes = new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP, Types.BIGINT, Types.DOUBLE, Types.TIMESTAMP, Types.BIGINT, Types.BIGINT, Types.VARCHAR};
    Object[]
        args =
        new Object[]{executionContextType.name(), type.name(), stringValue, dateValue,
                     longValue, doubleValue, TimeZoneUtil.getUTCTime(), executionId, stepExecutionId, key};
    this.getJdbcTemplate().update("UPDATE " + TABLE_NAME
                                  + " SET EXECUTION_CONTEXT_TYPE = ?, TYPE_CD = ?, STRING_VAL = ?, DATE_VAL = ?, LONG_VAL = ?, DOUBLE_VAL = ?, CREATE_DATE = ?"
                                  + "WHERE JOB_EXECUTION_ID = ? and STEP_EXECUTION_ID = ? AND KEY_NAME = ?",
                                  args, argTypes);
  }


}
