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

/**
 * Created by sr186054 on 4/13/16.
 */
public interface ExecutedFeed {

  String getName();

  void setName(String name);

  long getFeedInstanceId();

  void setFeedInstanceId(long feedInstanceId);

  long getFeedExecutionId();

  void setFeedExecutionId(long feedExecutionId);

  List<Throwable> getExceptions();

  void setExceptions(List<Throwable> exceptions);

  DateTime getEndTime();

  void setEndTime(DateTime endTime);

  String getExitCode();

  void setExitCode(String exitCode);

  String getExitStatus();

  void setExitStatus(String exitStatus);

  DateTime getStartTime();

  void setStartTime(DateTime startTime);

  ExecutionStatus getStatus();

  void setStatus(ExecutionStatus status);

  List<ExecutedJob> getExecutedJobs();

  void setExecutedJobs(List<ExecutedJob> executedJobs);

  Long getRunTime();

  void setRunTime(Long runTime);

  Long getTimeSinceEndTime();

  void setTimeSinceEndTime(Long timeSinceEndTime);

  boolean isLatest();

  void setIsLatest(boolean isLatest);
}
