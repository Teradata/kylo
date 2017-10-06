package com.thinkbiganalytics.jobrepo.query.model.transform;

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

import com.thinkbiganalytics.jobrepo.common.constants.CheckDataStepConstants;
import com.thinkbiganalytics.jobrepo.query.model.CheckDataJob;
import com.thinkbiganalytics.jobrepo.query.model.DefaultCheckDataJob;
import com.thinkbiganalytics.jobrepo.query.model.DefaultExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.DefaultExecutedStep;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedStep;
import com.thinkbiganalytics.jobrepo.query.model.ExecutionStatus;
import com.thinkbiganalytics.jobrepo.query.model.ExitStatus;
import com.thinkbiganalytics.metadata.api.feed.LatestFeedJobExecution;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventStepExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 */
public class JobModelTransform {


    public static ExecutedJob executedJob(BatchJobExecution jobExecution) {
        DefaultExecutedJob job = (DefaultExecutedJob) executedJobSimple(jobExecution);
        Map<String, String> jobExecutionContext = jobExecution.getJobExecutionContextAsMap();
        if (jobExecutionContext != null) {
            job.setExecutionContext(new HashMap<>(jobExecutionContext));
        }
        OpsManagerFeed feed = jobExecution.getJobInstance().getFeed();
        if (feed != null) {
            job.setJobType(feed.getFeedType().name());
        }
        Map<String, String> jobParams = jobExecution.getJobParametersAsMap();
        if (jobParams != null) {
            job.setJobParameters(new HashMap<>(jobParams));
        }

        job.setExecutedSteps(executedSteps(jobExecution.getStepExecutions()));
        return job;
    }

    public static ExecutedJob executedJobSimple(BatchJobExecution jobExecution) {
        DefaultExecutedJob job = new DefaultExecutedJob();
        job.setExecutionId(jobExecution.getJobExecutionId());
        job.setStartTime(jobExecution.getStartTime());
        job.setEndTime(jobExecution.getEndTime());
        job.setCreateTime(jobExecution.getCreateTime());
        job.setExitCode(jobExecution.getExitCode().name());
        job.setExitStatus(jobExecution.getExitMessage());
        job.setStatus(ExecutionStatus.valueOf(jobExecution.getStatus().name()));
        job.setJobName(jobExecution.getJobInstance().getJobName());
        job.setRunTime(ModelUtils.runTime(jobExecution.getStartTime(), jobExecution.getEndTime()));
        job.setTimeSinceEndTime(ModelUtils.timeSince(jobExecution.getStartTime(), jobExecution.getEndTime()));
        job.setInstanceId(jobExecution.getJobInstance().getJobInstanceId());
        job.setStream(jobExecution.isStream());
        if (jobExecution.getJobInstance() != null && jobExecution.getJobInstance().getFeed() != null) {
            job.setFeedName(jobExecution.getJobInstance().getFeed().getName());
            job.setFeedId(jobExecution.getJobInstance().getFeed().getId().toString());
        }
        return job;
    }

    public static ExecutedStep executedStep(BatchStepExecution stepExecution) {
        DefaultExecutedStep step = new DefaultExecutedStep();
        NifiEventStepExecution nifiEventStepExecution = stepExecution.getNifiEventStepExecution();
        if (nifiEventStepExecution != null) {
            step.setNifiEventId(nifiEventStepExecution.getEventId());
        }
        step.setRunning(!stepExecution.isFinished());
        step.setStartTime(stepExecution.getStartTime());
        step.setEndTime(stepExecution.getEndTime());
        step.setLastUpdateTime(stepExecution.getLastUpdated());
        step.setVersion(stepExecution.getVersion().intValue());
        step.setStepName(stepExecution.getStepName());
        step.setExitDescription(stepExecution.getExitMessage());
        step.setExitCode(stepExecution.getExitCode().name());
        step.setId(stepExecution.getStepExecutionId());
        step.setTimeSinceEndTime(ModelUtils.timeSince(stepExecution.getStartTime(), stepExecution.getEndTime()));
        step.setRunTime(ModelUtils.runTime(stepExecution.getStartTime(), stepExecution.getEndTime()));
        Map<String, String> stepExecutionContext = stepExecution.getStepExecutionContextAsMap();
        if (stepExecutionContext != null) {
            step.setExecutionContext(new HashMap<>(stepExecutionContext));
        }
        return step;

    }

    public static ExecutedJob executedJob(LatestFeedJobExecution jobExecution) {

        DefaultExecutedJob executedJob = new DefaultExecutedJob();
        executedJob.setExecutionId(jobExecution.getJobExecutionId());
        executedJob.setStartTime(jobExecution.getStartTime());
        executedJob.setEndTime(jobExecution.getEndTime());
        executedJob.setExitCode(jobExecution.getExitCode().name());
        executedJob.setExitStatus(jobExecution.getExitMessage());
        executedJob.setStatus(ExecutionStatus.valueOf(jobExecution.getStatus().name()));
        executedJob.setJobName(jobExecution.getFeedName());
        executedJob.setRunTime(ModelUtils.runTime(jobExecution.getStartTime(), jobExecution.getEndTime()));
        executedJob.setTimeSinceEndTime(ModelUtils.timeSince(jobExecution.getStartTime(), jobExecution.getEndTime()));
        executedJob.setInstanceId(jobExecution.getJobInstanceId());
        executedJob.setStream(jobExecution.isStream());
        executedJob.setFeedId(jobExecution.getFeedId());
        executedJob.setFeedName(jobExecution.getFeedName());
        return executedJob;

    }

    public static List<ExecutedStep> executedSteps(Collection<? extends BatchStepExecution> steps) {
        if (steps != null && !steps.isEmpty()) {
            return steps.stream().map(stepExecution -> executedStep(stepExecution)).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public static List<ExecutedJob> executedJobs(Collection<? extends BatchJobExecution> jobs) {
        if (jobs != null && !jobs.isEmpty()) {
            return jobs.stream().map(jobExecution -> executedJob(jobExecution)).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public static List<ExecutedJob> executedJobsSimple(Collection<? extends BatchJobExecution> jobs) {
        if (jobs != null && !jobs.isEmpty()) {
            return jobs.stream().map(jobExecution -> executedJobSimple(jobExecution)).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public static CheckDataJob checkDataJob(LatestFeedJobExecution latestFeedJobExecution) {
        ExecutedJob job = executedJob(latestFeedJobExecution);
        CheckDataJob checkDataJob = new DefaultCheckDataJob(job);
        boolean valid = false;
        String msg = "Unknown";
        Map<String, String> jobExecutionContext = latestFeedJobExecution.getJobExecution().getJobExecutionContextAsMap();
        if (BatchJobExecution.JobStatus.ABANDONED.equals(latestFeedJobExecution.getStatus())) {
            valid = true;
            msg = latestFeedJobExecution.getExitMessage();
        } else if (jobExecutionContext != null) {
            msg = jobExecutionContext.get(CheckDataStepConstants.VALIDATION_MESSAGE_KEY);
            String isValid = jobExecutionContext.get(CheckDataStepConstants.VALIDATION_KEY);
            if (msg == null) {
                msg = job.getExitStatus();
            }
            if (StringUtils.isBlank(isValid)) {
                valid = ExitStatus.COMPLETED.getExitCode().equals(job.getExitStatus());
            } else {
                valid = BooleanUtils.toBoolean(isValid);
            }
        }
        checkDataJob.setValidationMessage(msg);
        checkDataJob.setIsValid(valid);
        return checkDataJob;
    }

}
