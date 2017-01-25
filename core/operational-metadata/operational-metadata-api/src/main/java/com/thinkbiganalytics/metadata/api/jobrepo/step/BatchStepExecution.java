package com.thinkbiganalytics.metadata.api.jobrepo.step;

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
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventStepExecution;

import org.joda.time.DateTime;

import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 9/18/16.
 */
public interface BatchStepExecution {

    Long getStepExecutionId();

    Long getVersion();

    String getStepName();

    DateTime getStartTime();

    DateTime getEndTime();

    StepStatus getStatus();

    void setStatus(StepStatus status);

    BatchJobExecution getJobExecution();

    Set<BatchStepExecutionContextValue> getStepExecutionContext();

    Map<String, String> getStepExecutionContextAsMap();

    ExecutionConstants.ExitCode getExitCode();

    void setExitCode(ExecutionConstants.ExitCode exitCode);

    String getExitMessage();

    void setExitMessage(String msg);

    DateTime getLastUpdated();

    boolean isFinished();

    NifiEventStepExecution getNifiEventStepExecution();

    public enum StepStatus {
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
