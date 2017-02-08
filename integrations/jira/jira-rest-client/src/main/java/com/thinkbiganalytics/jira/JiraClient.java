package com.thinkbiganalytics.jira;

/*-
 * #%L
 * thinkbig-jira-rest-client
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

import com.thinkbiganalytics.jira.domain.Issue;
import com.thinkbiganalytics.jira.domain.ServerInfo;

import java.util.List;

/**
 */
public interface JiraClient {

    /**
     * Create a new Jira Issue
     *
     * @param projectKey   The String Key.  If you have issue  IT-1, then this is "IT"
     * @param issueType    the String type (i.e. "Bug", "Task")
     * @param assigneeName the jira username
     */
    Issue createIssue(String projectKey, String summary, String description, String issueType, String assigneeName) throws JiraException;


    /**
     * Create a new Jira Issue from an Issue object
     * Use IssueBuilder to create this initial issue object
     */
    Issue createIssue(Issue issue) throws JiraException;

    /**
     * Get a Jira Issue by its Key (i.e. JIRA-1)
     */
    Issue getIssue(String key) throws JiraException;


    /**
     * return a list of the IssueType names for a given project.
     * This is the name that is used when creating an Issue of a given type
     */
    List<String> getIssueTypeNamesForProject(String projectKey);


    /**
     * Gets the Server info
     */
    ServerInfo getServerInfo() throws JiraException;

    boolean isHostConfigured();
}
