package com.thinkbiganalytics.metadata.sla.alerts;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementActionConfig;
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;
import com.thinkbiganalytics.policy.PropertyLabelValue;

/**
 * Created by sr186054 on 7/20/16.
 */
@ServiceLevelAgreementActionConfig(
    name = "Jira",
    description = "Create a Jira when the SLA is violated",
    actionClasses = {JiraServiceLevelAgreementAction.class}
)
public class JiraServiceLevelAgreementActionConfiguration extends BaseServiceLevelAgreementActionConfiguration {

    @PolicyProperty(name = "JiraProjectKey",
                    displayName = "Jira Project Key",
                    hint = "The Jira Project Key ",
                    required = true)
    private String projectKey;
    @PolicyProperty(name = "JiraIssueType",
                    displayName = "Issue Type",
                    hint = "The Jira Type ",
                    value = "Bug", type = PolicyPropertyTypes.PROPERTY_TYPE.select,
                    labelValues = {@PropertyLabelValue(label = "Bug", value = "Bug"),
                                   @PropertyLabelValue(label = "Task", value = "Task")},
                    required = true)
    private String issueType;
    @PolicyProperty(name = "JiraAssignee", displayName = "Assignee", hint = "Who should get assigned this Jira? ", required = true)
    private String assignee;

    public JiraServiceLevelAgreementActionConfiguration() {

    }

    public JiraServiceLevelAgreementActionConfiguration(@PolicyPropertyRef(name = "JiraProjectKey") String projectKey, @PolicyPropertyRef(name = "JiraIssueType") String issueType,
                                                        @PolicyPropertyRef(name = "JiraAssignee") String assignee) {
        this.projectKey = projectKey;
        this.issueType = issueType;
        this.assignee = assignee;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }
}
