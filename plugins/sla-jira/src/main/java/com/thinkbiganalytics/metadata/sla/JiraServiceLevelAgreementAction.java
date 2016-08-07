package com.thinkbiganalytics.metadata.sla;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.classnameregistry.ClassNameChange;
import com.thinkbiganalytics.jira.JiraClient;
import com.thinkbiganalytics.jira.JiraException;
import com.thinkbiganalytics.jira.domain.Issue;
import com.thinkbiganalytics.jira.domain.IssueBuilder;
import com.thinkbiganalytics.metadata.sla.alerts.ServiceLevelAssessmentAlertUtil;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementAction;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * create the JIRA issue when  SLA is violated
 *
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


}
