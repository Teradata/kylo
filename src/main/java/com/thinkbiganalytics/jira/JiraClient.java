package com.thinkbiganalytics.jira;

import com.thinkbiganalytics.jira.domain.Issue;

/**
 * Created by sr186054 on 10/16/15.
 */
public interface JiraClient {

    Issue createIssue(String projectKey, String summary, String description, String issueType, String assigneeName) throws JiraException;
    Issue getIssue(String key) throws JiraException;

}
