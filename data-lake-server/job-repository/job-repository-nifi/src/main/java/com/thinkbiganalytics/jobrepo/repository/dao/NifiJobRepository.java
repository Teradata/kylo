package com.thinkbiganalytics.jobrepo.repository.dao;


import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileComponent;
import com.thinkbiganalytics.jobrepo.nifi.model.NifiJobExecution;

import java.util.Map;

/**
 * Created by sr186054 on 2/29/16.
 */
public interface NifiJobRepository {

  Long createJobInstance(NifiJobExecution nifiJobExecution);

  Long saveJobExecution(NifiJobExecution nifiJobExecution);

  void completeJobExecution(NifiJobExecution nifiJobExecution);

  void failJobExecution(NifiJobExecution nifiJobExecution);

  Long saveStepExecution(FlowFileComponent flowFileComponent);

  void completeStep(FlowFileComponent flowFileComponent);

  void failStep(FlowFileComponent flowFileComponent);

  void saveStepExecutionContext(FlowFileComponent flowFileComponent, Map<String, Object> attrs);

  void saveJobExecutionContext(NifiJobExecution jobExecution, Map<String, Object> attrs);

  Long getLastEventIdProcessedByPipelineController();

  void setAsCheckDataJob(Long jobExecutionId, String feedName);

}
