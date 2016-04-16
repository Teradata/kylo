/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.nifi.support;

import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileComponent;
import com.thinkbiganalytics.jobrepo.nifi.model.NifiJobExecution;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sr186054 on 3/1/16.
 */
public class NifiSpringBatchTransformer {

  public static JobExecution getJobExecution(NifiJobExecution nifiJobExecution) {

    Map<String, JobParameter> params = new HashMap<>();
    for (Map.Entry<String, String> entry : nifiJobExecution.getJobParameters().entrySet()) {
      params.put(entry.getKey(), new JobParameter(entry.getValue()));
    }
    JobParameters jobParameters = new JobParameters(params);
    JobExecution jobExecution = new JobExecution((Long) null, jobParameters);
    jobExecution.setJobInstance(new JobInstance(nifiJobExecution.getJobInstanceId(), nifiJobExecution.getFeedName()));
    jobExecution.setStartTime(nifiJobExecution.getUTCStartTime());
    jobExecution.setEndTime(nifiJobExecution.getUTCEndTime());
    jobExecution.setStatus(BatchStatus.valueOf(nifiJobExecution.getStatus().toString()));
    jobExecution.setExitStatus(nifiJobExecution.getExitStatus());
    jobExecution.setVersion(nifiJobExecution.getVersion());
    jobExecution.setCreateTime(nifiJobExecution.getCreateTime());
    jobExecution.setLastUpdated(nifiJobExecution.getLastUpdated());
    jobExecution.setId(nifiJobExecution.getJobExecutionId());
    return jobExecution;

  }

  public static StepExecution populateStepExecution(StepExecution stepExecution, FlowFileComponent component) {
    stepExecution.setStartTime(component.getUTCStartTime());
    stepExecution.setEndTime(component.getUTCEndTime());
    stepExecution.setVersion(component.getVersion());
    stepExecution.setId(component.getStepExecutionId());

    return stepExecution;

  }
}
