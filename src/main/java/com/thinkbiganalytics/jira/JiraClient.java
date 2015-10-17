package com.thinkbiganalytics.jira;

import com.thinkbiganalytics.jira.domain.Issue;

/**
 * Created by sr186054 on 10/16/15.
 */
public interface JiraClient {

    /**
     * Create a new Jira Issue
     * @param projectKey The String Key.  If you have issue  IT-1, then this is "IT"
     * @param summary
     * @param description
     * @param issueType  the String type (i.e. "Bug", "Task")
     * @param assigneeName  the jira username
     * @return
     * @throws JiraException
     */
    Issue createIssue(String projectKey, String summary, String description, String issueType, String assigneeName) throws JiraException;

    /**
     * Get a Jira Issue by its Key (i.e. JIRA-1)
     * @param key
     * @return
     * @throws JiraException
     */
    Issue getIssue(String key) throws JiraException;

}
