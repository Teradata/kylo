package com.thinkbiganalytics.jobrepo.query.rowmapper;


import com.thinkbiganalytics.jobrepo.query.job.CheckDataAllJobsQuery;
import com.thinkbiganalytics.jobrepo.query.model.CheckDataJob;
import com.thinkbiganalytics.jobrepo.query.model.DefaultCheckDataJob;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.repository.JobRepositoryImpl;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.batch.core.JobExecution;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by sr186054 on 8/28/15.
 */
public class CheckDataJobRowMapper implements RowMapper<CheckDataJob> {

  private JobExecutionFeedRowMapper executedFeedRowMapper;

  public CheckDataJobRowMapper() {
    executedFeedRowMapper = new JobExecutionFeedRowMapper();

  }


  @Override
  public CheckDataJob mapRow(ResultSet resultSet, int i) throws SQLException {
    JobExecution jobExecution = executedFeedRowMapper.mapRow(resultSet, i);
    ExecutedJob job = JobRepositoryImpl.convertToExecutedJob(jobExecution.getJobInstance(), jobExecution);
    CheckDataJob checkDataJob = new DefaultCheckDataJob(job);
    checkDataJob.setIsValid(BooleanUtils.toBoolean(resultSet.getString(CheckDataAllJobsQuery.QUERY_IS_VALID)));
    checkDataJob.setValidationMessage(resultSet.getString(CheckDataAllJobsQuery.QUERY_VALIDATION_MESSAGE));
    checkDataJob.setFeedName(resultSet.getString(CheckDataAllJobsQuery.QUERY_FEED_NAME));
    return checkDataJob;
  }
}
