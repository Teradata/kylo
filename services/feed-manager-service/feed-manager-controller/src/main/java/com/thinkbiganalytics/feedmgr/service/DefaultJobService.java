package com.thinkbiganalytics.feedmgr.service;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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


import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.service.JobExecutionException;
import com.thinkbiganalytics.jobrepo.service.JobService;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.feed.FeedOperationStatusEvent;
import com.thinkbiganalytics.metadata.api.event.feed.OperationStatus;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import javax.inject.Inject;

/**
 * Service to perform actions on batch job executions
 */
public class DefaultJobService implements JobService {

    private static final Logger log = LoggerFactory.getLogger(DefaultJobService.class);

    @Inject
    private MetadataAccess metadataAccess;

    @Autowired
    private BatchJobExecutionProvider jobExecutionProvider;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    private LegacyNifiRestClient nifiRestClient;

    @Override
    public Long restartJobExecution(Long executionId) throws JobExecutionException {
        //not supported now
        return null;

    }


    public boolean canReplay(ProvenanceEventDTO event) {
        return event.getReplayAvailable() != null ? event.getReplayAvailable().booleanValue() : false;
    }

    @Override
    public boolean stopJobExecution(Long executionId) throws JobExecutionException {
        throw new UnsupportedOperationException("Unable to stop Nifi Job Execution at this time.  Please mark the job as Failed and Abandoned, if necessary.");
    }

    @Override
    public void abandonJobExecution(Long executionId) throws JobExecutionException {
        metadataAccess.commit(() -> {
          return  this.jobExecutionProvider.abandonJob(executionId);
        });
    }

    @Override
    public void failJobExecution(Long executionId) {
        metadataAccess.commit(() -> {

            BatchJobExecution execution = this.jobExecutionProvider.findByJobExecutionId(executionId);
            if (execution != null && !execution.isFailed()) {
                Set<BatchStepExecution> steps = execution.getStepExecutions();
                if (steps != null) {
                    for (BatchStepExecution step : steps) {
                        if (!step.isFinished()) {
                            step.setStatus(BatchStepExecution.StepStatus.FAILED);
                            step.setExitCode(ExecutionConstants.ExitCode.FAILED);
                            String msg = step.getExitMessage() != null ? step.getExitMessage() + "\n" : "";
                            msg += "Step manually failed @ " + DateTimeUtil.getNowFormattedWithTimeZone();
                            step.setExitMessage(msg);
                            execution.setExitMessage(msg);
                        }
                    }
                }
                if (execution.getStartTime() == null) {
                    execution.setStartTime(DateTimeUtil.getNowUTCTime());
                }
                execution.setStatus(BatchJobExecution.JobStatus.FAILED);
                if (execution.getEndTime() == null) {
                    execution.setEndTime(DateTimeUtil.getNowUTCTime());
                }
                String msg = execution.getExitMessage() != null ? execution.getExitMessage() + "\n" : "";
                msg += "Job manually failed @ " + DateTimeUtil.getNowFormattedWithTimeZone();
                execution.setExitMessage(msg);
                OpsManagerFeed feed = execution.getJobInstance().getFeed();
                this.jobExecutionProvider.save(execution);
                this.jobExecutionProvider.notifyFailure(execution,feed,false,"Job manually failed @ " + DateTimeUtil.getNowFormattedWithTimeZone());

            }
            return execution;
        });
    }


}
