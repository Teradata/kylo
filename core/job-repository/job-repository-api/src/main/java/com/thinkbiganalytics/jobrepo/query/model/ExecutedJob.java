package com.thinkbiganalytics.jobrepo.query.model;

/*-
 * #%L
 * thinkbig-job-repository-api
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface ExecutedJob {

  long getInstanceId();

  void setInstanceId(long instanceId);

  long getExecutionId();

  void setExecutionId(long executionId);

  void setJobId(Long jobId);

  String getJobName();

  void setJobName(String jobName);

  List<Throwable> getExceptions();

  void setExceptions(List<Throwable> exceptions);

  DateTime getCreateTime();

  void setCreateTime(DateTime createTime);

  DateTime getEndTime();

  void setEndTime(DateTime endTime);

  Map<String, Object> getExecutionContext();

  void setExecutionContext(Map<String, Object> executionContext);

  String getExitCode();

  void setExitCode(String exitCode);

  String getExitStatus();

  void setExitStatus(String exitStatus);

  String getJobConfigurationName();

  void setJobConfigurationName(String jobConfigurationName);

  Long getJobId();

  Map<String, Object> getJobParameters();

  void setJobParameters(Map<String, Object> jobParameters);

  DateTime getLastUpdated();

  void setLastUpdated(DateTime lastUpdated);

  DateTime getStartTime();

  void setStartTime(DateTime startTime);

  ExecutionStatus getStatus();

  void setStatus(ExecutionStatus status);

  List<ExecutedStep> getExecutedSteps();

  void setExecutedSteps(List<ExecutedStep> executedSteps);

  Long getRunTime();

  void setRunTime(Long runTime);

  Long getTimeSinceEndTime();

  void setTimeSinceEndTime(Long timeSinceEndTime);

  String getJobType();

  void setJobType(String jobType);

  boolean isLatest();

  void setIsLatest(boolean isLatest);

  String getFeedName();

  void setFeedName(String feedName);

  String getDisplayStatus();
}
