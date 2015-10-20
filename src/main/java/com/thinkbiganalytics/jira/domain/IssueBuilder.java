package com.thinkbiganalytics.jira.domain;

/**
 * Created by sr186054 on 10/19/15.
 */
public class IssueBuilder {
    private String projectKey;
    private String issueTypeName;
    private String summary;
    private String description;
    private String assignee;

    public IssueBuilder(String projectKey, String issueTypeName){
        this.projectKey = projectKey;
        this.issueTypeName = issueTypeName;
    }

    public IssueBuilder setSummary(String summary){
        this.summary = summary;
        return this;
    }

    public IssueBuilder setDescription(String description){
        this.description = description;
        return this;
    }

    public IssueBuilder setAssignee(String assignee){
        this.assignee = assignee;
        return this;
    }

    public Issue build(){
        return new Issue(projectKey,issueTypeName,summary,description,assignee);
    }

}
