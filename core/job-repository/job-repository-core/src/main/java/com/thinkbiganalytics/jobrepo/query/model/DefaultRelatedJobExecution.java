package com.thinkbiganalytics.jobrepo.query.model;

/*-
 * #%L
 * thinkbig-job-repository-core
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

import java.util.Date;

/**
 * Created by sr186054 on 12/10/15.
 */
public class DefaultRelatedJobExecution implements RelatedJobExecution {

  private Long jobExecutionId;
  private String jobName;
  private Date startTime;
  private Date endTime;


  @Override
  public Long getJobExecutionId() {
    return jobExecutionId;
  }

  @Override
  public void setJobExecutionId(Long jobExecutionId) {
    this.jobExecutionId = jobExecutionId;
  }

  @Override
  public String getJobName() {
    return jobName;
  }

  @Override
  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  @Override
  public Date getStartTime() {
    return startTime;
  }

  @Override
  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  @Override
  public Date getEndTime() {
    return endTime;
  }

  @Override
  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }
}
