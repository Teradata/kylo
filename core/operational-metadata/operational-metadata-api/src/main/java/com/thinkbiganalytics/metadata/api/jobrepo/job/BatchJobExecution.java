package com.thinkbiganalytics.metadata.api.jobrepo.job;

/*-
 * #%L
 * thinkbig-operational-metadata-api
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

import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;

import org.joda.time.DateTime;

import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface BatchJobExecution {

    BatchJobInstance getJobInstance();

    Long getJobExecutionId();

    Long getVersion();

    DateTime getCreateTime();

    void setCreateTime(DateTime createTime);

    DateTime getStartTime();

    void setStartTime(DateTime startTime);

    DateTime getEndTime();

    void setEndTime(DateTime endTime);

    JobStatus getStatus();

    void setStatus(JobStatus status);

    ExecutionConstants.ExitCode getExitCode();

    String getExitMessage();

    void setExitMessage(String exitMessage);

    DateTime getLastUpdated();

    Set<? extends BatchJobExecutionParameter> getJobParameters();

    Map<String, String> getJobParametersAsMap();

    Set<BatchStepExecution> getStepExecutions();

    Set<BatchJobExecutionContextValue> getJobExecutionContext();

    Map<String, String> getJobExecutionContextAsMap();

    NifiEventJobExecution getNifiEventJobExecution();

    void setNifiEventJobExecution(NifiEventJobExecution nifiEventJobExecution);

    boolean isFailed();

    boolean isSuccess();

    boolean isFinished();

    public static enum JobStatus {
        COMPLETED,
        STARTING,
        STARTED,
        STOPPING,
        STOPPED,
        FAILED,
        ABANDONED,
        UNKNOWN;
    }

}
