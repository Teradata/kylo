package com.thinkbiganalytics.jira.domain;

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

/**
 */
public class IssueBuilder {

    private String projectKey;
    private String issueTypeName;
    private String summary;
    private String description;
    private String assignee;

    public IssueBuilder(String projectKey, String issueTypeName) {
        this.projectKey = projectKey;
        this.issueTypeName = issueTypeName;
    }

    public IssueBuilder setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public IssueBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public IssueBuilder setAssignee(String assignee) {
        this.assignee = assignee;
        return this;
    }

    public Issue build() {
        return new Issue(projectKey, issueTypeName, summary, description, assignee);
    }

}
