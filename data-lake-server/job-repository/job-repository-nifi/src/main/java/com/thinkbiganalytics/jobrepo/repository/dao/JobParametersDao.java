/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.repository.dao;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao;

import java.sql.Timestamp;

/**
 * Created by sr186054 on 4/5/16.
 */
public class JobParametersDao extends AbstractJdbcBatchMetadataDao {




    private void insertParameter(Long executionId, JobParameter.ParameterType type, String key, Object value, boolean identifying) {
        Object[] args = new Object[0];
        int[] argTypes = new int[]{-5, 12, 12, 12, 93, -5, 8, 1};
        String identifyingFlag = identifying?"Y":"N";
        if(type == JobParameter.ParameterType.STRING) {
            args = new Object[]{executionId, key, type, value, new Timestamp(0L), Long.valueOf(0L), Double.valueOf(0.0D), identifyingFlag};
        } else if(type == JobParameter.ParameterType.LONG) {
            args = new Object[]{executionId, key, type, "", new Timestamp(0L), value, new Double(0.0D), identifyingFlag};
        } else if(type == JobParameter.ParameterType.DOUBLE) {
            args = new Object[]{executionId, key, type, "", new Timestamp(0L), Long.valueOf(0L), value, identifyingFlag};
        } else if(type == JobParameter.ParameterType.DATE) {
            args = new Object[]{executionId, key, type, "", value, Long.valueOf(0L), Double.valueOf(0.0D), identifyingFlag};
        }

        this.getJdbcTemplate().update(this.getQuery("INSERT into %PREFIX%JOB_EXECUTION_PARAMS(JOB_EXECUTION_ID, KEY_NAME, TYPE_CD, STRING_VAL, DATE_VAL, LONG_VAL, DOUBLE_VAL, IDENTIFYING) values (?, ?, ?, ?, ?, ?, ?, ?)"), args, argTypes);
    }


    public int updateJobParameter(Long executionId, String key, Object value,JobParameter.ParameterType type) {

        Object[] args = new Object[0];
        int[] argTypes = new int[]{ 12, 12, 93, -5, 8, -5, 12};
        if(type == JobParameter.ParameterType.STRING) {
            args = new Object[]{type, value, new Timestamp(0L), Long.valueOf(0L), Double.valueOf(0.0D), executionId, key};
        } else if(type == JobParameter.ParameterType.LONG) {
            args = new Object[]{ type, "", new Timestamp(0L), value, new Double(0.0D), executionId, key};
        } else if(type == JobParameter.ParameterType.DOUBLE) {
            args = new Object[]{ type, "", new Timestamp(0L), Long.valueOf(0L), value, executionId, key};
        } else if(type == JobParameter.ParameterType.DATE) {
            args = new Object[]{type, "", value, Long.valueOf(0L), Double.valueOf(0.0D), executionId, key};
        }

      return  this.getJdbcTemplate().update(this.getQuery("UPDATE %PREFIX%JOB_EXECUTION_PARAMS SET TYPE_CD = ?, STRING_VAL = ?, DATE_VAL = ?, LONG_VAL = ?, DOUBLE_VAL = ? WHERE JOB_EXECUTION_ID = ? and  KEY_NAME = ?"), args, argTypes);
    }

}
