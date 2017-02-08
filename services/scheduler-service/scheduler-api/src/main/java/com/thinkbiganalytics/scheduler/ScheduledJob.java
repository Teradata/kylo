package com.thinkbiganalytics.scheduler;

/*-
 * #%L
 * thinkbig-scheduler-api
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
import java.util.List;

/**
 * Represents a job that is scheduled in the {@link JobScheduler}
 */
public interface ScheduledJob {

    String getJobStatus();

    void setJobStatus(String jobStatus);

    String getJobGroup();

    void setJobGroup(String jobGroup);

    String getNextFireTimeString();

    void setNextFireTimeString(String nextFireTimeString);

    Date getNextFireTime();

    void setNextFireTime(Date nextFireTime);

    String getCronExpression();

    void setCronExpression(String cronExpression);

    String getJobName();

    void setJobName(String jobName);

    List<TriggerInfo> getTriggers();

    void setTriggers(List<TriggerInfo> triggers);

    JobIdentifier getJobIdentifier();

    void setJobIdentifier(JobIdentifier jobIdentifier);

    void setState();

    String getState();

    String getCronExpressionSummary();

    void setCronExpressionData();

    boolean isRunning();

    boolean isPaused();

    boolean isScheduled();
}
