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

import com.google.common.base.MoreObjects;
import com.thinkbiganalytics.jira.domain.util.UriUtil;

import org.joda.time.DateTime;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class Issue extends BasicIssue {


    private Status status;
    private IssueType issueType;
    private Project project;
    private URI transitionsUri;
    private Collection<Component> components;
    private String summary;
    private String description;
    private User reporter;
    private User assignee;
    private Resolution resolution;
    private DateTime creationDate;
    private DateTime updateDate;
    private DateTime dueDate;
    private Priority priority;
    private Votes votes;
    private Collection<Version> fixVersions;
    private Collection<Version> affectedVersions;

    private Collection<Comment> comments;

    private Collection<IssueLink> issueLinks;

    //private  Collection<Worklog> worklogs;
    //   private  BasicWatchers watchers;

    private TimeTracking timeTracking;
    // private  Collection<Subtask> subtasks;
    // private  Collection<ChangelogGroup> changelog;
    // private  Operations operations;
    private Set<String> labels;


    public Issue(String projectKey, String issueTypeName, String summary, String description) {
        Project project = new Project();
        project.setKey(projectKey);

        IssueType issueType = new IssueType();
        issueType.setName(issueTypeName);
        this.project = project;
        this.issueType = issueType;
        this.description = description;
        this.summary = summary;
    }


    public Issue(String projectKey, String issueTypeName, String summary, String description, String assigneeName) {
        this(projectKey, issueTypeName, summary, description);
        User user = new User();
        user.setName(assigneeName);
        this.setAssignee(user);
    }

    public Issue(GetIssue getIssue) {
        super(getIssue);
        this.status = getIssue.getFields().getStatus();
        this.issueType = getIssue.getFields().getIssueType();
        this.project = getIssue.getFields().getProject();
        this.components = getIssue.getFields().getComponents();
        this.summary = getIssue.getFields().getSummary();
        this.description = getIssue.getFields().getDescription();
        this.reporter = getIssue.getFields().getReporter();
        this.assignee = getIssue.getFields().getAssignee();
        this.resolution = getIssue.getFields().getResolution();
        this.creationDate = getIssue.getFields().getCreated();
        this.updateDate = getIssue.getFields().getUpdated();
        this.dueDate = getIssue.getFields().getDuedate();
        this.priority = getIssue.getFields().getPriority();
        this.votes = getIssue.getFields().getVotes();
        this.fixVersions = getIssue.getFields().getFixVersions();
        this.affectedVersions = getIssue.getFields().getAffectedVersions();
        if (getIssue.getFields().getComments() != null) {
            this.comments = getIssue.getFields().getComments().getComments();
        }
        this.issueLinks = getIssue.getFields().getIssuelinks();
        this.timeTracking = getIssue.getFields().getTimetracking();
        if (getIssue.getFields().getLabels() != null) {
            this.labels = new HashSet(getIssue.getFields().getLabels());
        }
    }


    public Issue(URI self, String key, Long id) {
        super(self, key, id);
    }


    public Status getStatus() {
        return status;
    }

    /**
     * @return reporter of this issue or <code>null</code> if this issue has no reporter
     */

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    /**
     * @return assignee of this issue or <code>null</code> if this issue is unassigned.
     */

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @return priority of this issue
     */

    public Priority getPriority() {
        return priority;
    }

    /**
     * @return issue links for this issue (possibly nothing) or <code>null</code> when issue links are deactivated for this JIRA instance
     */

    public Iterable<IssueLink> getIssueLinks() {
        return issueLinks;
    }

    /**
     * @return issue type
     */
    public IssueType getIssueType() {
        return issueType;
    }

    /**
     * @return comments for this issue
     */
    public Iterable<Comment> getComments() {
        return comments;
    }

    public URI getCommentsUri() {
        return UriUtil.path(getSelf(), "comment");
    }

    /**
     * @return project this issue belongs to
     */
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * @return <code>null</code when voting is disabled in JIRA
     */

    public Votes getVotes() {
        return votes;
    }

    public Iterable<Version> getFixVersions() {
        return fixVersions;
    }

    public URI getTransitionsUri() {
        return transitionsUri;
    }

    public Iterable<Version> getAffectedVersions() {
        return affectedVersions;
    }

    public Iterable<Component> getComponents() {
        return components;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public URI getVotesUri() {
        return UriUtil.path(getSelf(), "votes");
    }

    public Resolution getResolution() {
        return resolution;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public DateTime getUpdateDate() {
        return updateDate;
    }

    public DateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(DateTime dueDate) {
        this.dueDate = dueDate;
    }

    public TimeTracking getTimeTracking() {
        return timeTracking;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    protected MoreObjects.ToStringHelper getToStringHelper() {
        return super.getToStringHelper().
            add("project", project).
            add("status", status).
            add("description", description).
            add("reporter", reporter).
            add("assignee", assignee).addValue("\n").
            add("issueType", issueType).
            add("creationDate", creationDate);

    }
}
