package com.thinkbiganalytics.metadata.jpa.jobrepo.job;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertResponder;
import com.thinkbiganalytics.alerts.api.AlertResponse;
import com.thinkbiganalytics.alerts.sla.AssessmentAlerts;
import com.thinkbiganalytics.jobrepo.service.JobService;
import com.thinkbiganalytics.metadata.api.alerts.OperationalAlerts;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Created by sr186054 on 7/19/17.
 */
public class JobFailureAlertResponder implements AlertResponder {

    private static final Logger log = LoggerFactory.getLogger(JobFailureAlertResponder.class);


    @Inject
    JobService jobService;

    @Override
    public void alertChange(Alert alert, AlertResponse response) {
        if (OperationalAlerts.isFeedJobFailureType(alert.getType())) {
        if (alert.getEvents().get(0).getState() == Alert.State.HANDLED) {
                    abandonJob(alert,response);
            }
        }

    }


    private void abandonJob(Alert alert,AlertResponse response) {
        try {
            response.inProgress("Handling Job Failure Alert");
            Long jobExecutionId = alert.getContent();
            jobService.abandonJobExecution(jobExecutionId);
            response.handle("Handled Job Failure Alert");
        } catch (Exception e) {
            log.error("ERROR Handling Alert Error {} ", e.getMessage());
            response.unhandle("Failed to handle violation: " + e.getMessage());
        }
    }
}
