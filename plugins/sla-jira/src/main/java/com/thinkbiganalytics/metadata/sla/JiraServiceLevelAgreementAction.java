package com.thinkbiganalytics.metadata.sla;

/*-
 * #%L
 * thinkbig-sla-jira
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
import com.thinkbiganalytics.classnameregistry.ClassNameChange;
import com.thinkbiganalytics.jira.JiraClient;
import com.thinkbiganalytics.jira.JiraException;
import com.thinkbiganalytics.jira.domain.Issue;
import com.thinkbiganalytics.jira.domain.IssueBuilder;
import com.thinkbiganalytics.metadata.sla.alerts.ServiceLevelAssessmentAlertUtil;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionValidation;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * create the JIRA issue when  SLA is violated
 */
@ClassNameChange(classNames = {"com.thinkbiganalytics.metadata.sla.alerts.JiraServiceLevelAgreementAction"})
public class JiraServiceLevelAgreementAction implements ServiceLevelAgreementAction<JiraServiceLevelAgreementActionConfiguration> {

    private static final Logger log = LoggerFactory.getLogger(JiraServiceLevelAgreementAction.class);
    @Inject
    private JiraClient jiraClient;

    public void setJiraClient(JiraClient jiraClient) {
        this.jiraClient = jiraClient;
    }

    @Override
    public boolean respond(JiraServiceLevelAgreementActionConfiguration actionConfiguration, ServiceLevelAssessment assessment, Alert a) {
        String desc = ServiceLevelAssessmentAlertUtil.getDescription(assessment);
        String projectKey = actionConfiguration.getProjectKey();
        String issueType = actionConfiguration.getIssueType();
        String assignee = actionConfiguration.getAssignee();

        if (jiraClient.isHostConfigured()) {
            Issue issue = new IssueBuilder(projectKey, issueType)
                .setAssignee(assignee)
                .setDescription(desc)
                .setSummary("JIRA for " + assessment.getAgreement().getName())
                .build();
            log.info("Generating Jira issue: \"{}\"", issue.getSummary());
            log.debug("Jira description: {}", issue.getDescription());
            try {
                jiraClient.createIssue(issue);
            } catch (JiraException e) {
                log.error("Unable to create Jira Issue ", e);
            }
        } else {
            log.debug("Jira is not configured.  Issue will not be generated: \"{}\"", desc);
        }
        return true;
    }

    public ServiceLevelAgreementActionValidation validateConfiguration() {
        boolean configured = jiraClient.isHostConfigured();
        if (configured) {
            return ServiceLevelAgreementActionValidation.VALID;
        } else {
            return new ServiceLevelAgreementActionValidation(false, "JIRA connection information is not setup.  Please contact an administrator to set this up.");
        }
    }

}
