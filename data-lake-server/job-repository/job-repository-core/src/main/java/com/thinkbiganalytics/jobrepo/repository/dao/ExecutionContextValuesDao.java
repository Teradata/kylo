package com.thinkbiganalytics.jobrepo.repository.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Responsible for taking a ExecutionContext (either Step or Job) and persisting the values in the Context map to
 * the custom BATCH_EXECUTION_CONTEXT_VALUES table
 *
 * Created by sr186054 on 8/27/15
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
     *
     * @param stepExecution
     */
    public void saveStepExecutionContextValues(StepExecution stepExecution) {
        saveStepExecutionContextValues(stepExecution, null);
    }

    /**
     * Save specific parameters matching the keys passed in via the List
     *
     * @param stepExecution
     * @param stepExecutionKeys
     */
    public void saveStepExecutionContextValues(StepExecution stepExecution, List<String> stepExecutionKeys) {
        Long jobExecutionId = stepExecution.getJobExecutionId();
        saveExecutionContextValues(jobExecutionId, stepExecution.getId(), ExecutionContextType.STEP, stepExecution.getExecutionContext(), stepExecutionKeys);
    }

    /**
     * Save all the parameters in the JobExecution.ExecutionContext to the table
     *
     * @param jobExecution
     */
    public void saveJobExecutionContextValues(JobExecution jobExecution) {
        saveJobExecutionContextValues(jobExecution, null);
    }

    /**
     * Save specific parameters matching the keys passed in via the List for the JobExecution.ExecutionContext
     *
     * @param jobExecution
     * @param jobExecutionKeys
     */
    public void saveJobExecutionContextValues(JobExecution jobExecution, List<String> jobExecutionKeys) {
        Long jobExecutionId = jobExecution.getId();
        saveExecutionContextValues(jobExecutionId, null, ExecutionContextType.JOB, jobExecution.getExecutionContext(), jobExecutionKeys);
    }

    private void saveExecutionContextValues(Long jobExecutionId, Long stepExecutionId, ExecutionContextType executionContextType, ExecutionContext executionContext, List<String> stepExecutionKeys) {

        for (Map.Entry<String, Object> params : executionContext.entrySet()) {
            String key = params.getKey();
            Object value = params.getValue();
            if (stepExecutionKeys == null || (stepExecutionKeys != null && stepExecutionKeys.contains(key))) {
                insertParameter(jobExecutionId, stepExecutionId, executionContextType, key, value);
            }
        }
    }


    private void insertParameter(Long executionId, Long stepExecutionId, ExecutionContextType executionContextType, String key, Object value) {
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

        int[] argTypes = new int[]{-5, -5, 12, 12, 12, 12, 93, -5, 8, 93};
        Object[] args = new Object[]{executionId, stepExecutionId, executionContextType.name(), key, type.name(), stringValue, dateValue, longValue, doubleValue, new Date()};
        this.getJdbcTemplate().update("INSERT into " + TABLE_NAME + "(JOB_EXECUTION_ID,STEP_EXECUTION_ID,EXECUTION_CONTEXT_TYPE,KEY_NAME, TYPE_CD, STRING_VAL, DATE_VAL, LONG_VAL, DOUBLE_VAL, CREATE_DATE) values (?,?,?, ?, ?, ?, ?, ?, ?,?)", args, argTypes);
    }


}
