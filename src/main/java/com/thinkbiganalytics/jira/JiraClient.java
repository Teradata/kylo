package com.thinkbiganalytics.jira;

import com.thinkbiganalytics.jira.domain.Issue;

import java.util.List;

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
     * Create a new Jira Issue from an Issue object
     * Use IssueBuilder to create this initial issue object
     * @param issue
     * @return
     * @throws JiraException
     */
    Issue createIssue(Issue issue) throws JiraException;

    /**
     * Get a Jira Issue by its Key (i.e. JIRA-1)
     * @param key
     * @return
     * @throws JiraException
     */
    Issue getIssue(String key) throws JiraException;



    /**
     * return a list of the IssueType names for a given project.
     * This is the name that is used when creating an Issue of a given type
     * @param projectKey
     * @return
     */
    List<String> getIssueTypeNamesForProject(String projectKey);

}
