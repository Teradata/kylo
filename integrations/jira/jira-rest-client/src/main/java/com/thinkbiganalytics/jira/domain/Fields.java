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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
                       "fixVersions",
                       "affectedVersions",
                       "resolution",
                       "lastViewed",
                       "priority",
                       "labels",
                       "timeestimate",
                       "versions",
                       "issuelinks",
                       "assignee",
                       "status",
                       "components",
                       "creator",
                       "subtasks",
                       "reporter",
                       "progress",
                       "votes",
                       "worklog",
                       "issuetype",
                       "project",
                       "watches",
                       "created",
                       "updated",
                       "description",
                       "timetracking",
                       "attachment",
                       "summary",
                       "duedate",
                       "comment"
                   })
public class Fields {


    @JsonProperty("fixVersions")
    private List<Version> fixVersions = new ArrayList<Version>();
    @JsonProperty("affectedVersions")
    private List<Version> affectedVersions = new ArrayList<Version>();
    @JsonProperty("resolution")
    private Resolution resolution;
    @JsonProperty("lastViewed")
    private String lastViewed;
    @JsonProperty("priority")
    private Priority priority;
    @JsonProperty("labels")
    private List<String> labels = new ArrayList<String>();
    @JsonProperty("versions")
    private List<Version> versions = new ArrayList<Version>();
    @JsonProperty("issuelinks")
    private List<IssueLink> issuelinks = new ArrayList<IssueLink>();
    @JsonProperty("assignee")
    private User assignee;
    @JsonProperty("status")
    private Status status;
    @JsonProperty("components")
    private List<Component> components = new ArrayList<Component>();
    @JsonProperty("creator")
    private User creator;
    @JsonProperty("subtasks")
    private List<Object> subtasks = new ArrayList<Object>();
    @JsonProperty("reporter")
    private User reporter;
    @JsonProperty("progress")
    private Progress progress;
    @JsonProperty("votes")
    private Votes votes;
    @JsonProperty("worklog")
    private Worklog worklog;
    @JsonProperty("issuetype")
    private IssueType issueType;
    @JsonProperty("project")
    private Project project;
    @JsonProperty("watches")
    private Watches watches;
    @JsonProperty("created")
    private DateTime created;
    @JsonProperty("updated")
    private DateTime updated;
    @JsonProperty("description")
    private String description;
    @JsonProperty("timetracking")
    private TimeTracking timetracking;
    @JsonProperty("attachment")
    private List<Object> attachment = new ArrayList<Object>();
    @JsonProperty("summary")
    private String summary;
    @JsonProperty("duedate")
    private DateTime duedate;
    @JsonProperty("comment")
    private Comments comments;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


    /**
     * @return The fixVersions
     */
    @JsonProperty("fixVersions")
    public List<Version> getFixVersions() {
        return fixVersions;
    }

    /**
     * @param fixVersions The fixVersions
     */
    @JsonProperty("fixVersions")
    public void setFixVersions(List<Version> fixVersions) {
        this.fixVersions = fixVersions;
    }


    /**
     * @return The affectedVersions
     */
    @JsonProperty("affectedVersions")
    public List<Version> getAffectedVersions() {
        return affectedVersions;
    }

    /**
     * @param affectedVersions The affectedVersions
     */
    @JsonProperty("affectedVersions")
    public void setAffectedVersions(List<Version> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }

    /**
     * @return The resolution
     */
    @JsonProperty("resolution")
    public Resolution getResolution() {
        return resolution;
    }

    /**
     * @param resolution The resolution
     */
    @JsonProperty("resolution")
    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    /**
     * @return The lastViewed
     */
    @JsonProperty("lastViewed")
    public String getLastViewed() {
        return lastViewed;
    }

    /**
     * @param lastViewed The lastViewed
     */
    @JsonProperty("lastViewed")
    public void setLastViewed(String lastViewed) {
        this.lastViewed = lastViewed;
    }

    /**
     * @return The priority
     */
    @JsonProperty("priority")
    public Priority getPriority() {
        return priority;
    }

    /**
     * @param priority The priority
     */
    @JsonProperty("priority")
    public void setPriority(Priority priority) {
        this.priority = priority;
    }


    /**
     * @return The labels
     */
    @JsonProperty("labels")
    public List<String> getLabels() {
        return labels;
    }

    /**
     * @param labels The labels
     */
    @JsonProperty("labels")
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    /**
     * @return The versions
     */
    @JsonProperty("versions")
    public List<Version> getVersions() {
        return versions;
    }

    /**
     * @param versions The versions
     */
    @JsonProperty("versions")
    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }

    /**
     * @return The issuelinks
     */
    @JsonProperty("issuelinks")
    public List<IssueLink> getIssuelinks() {
        return issuelinks;
    }

    /**
     * @param issuelinks The issuelinks
     */
    @JsonProperty("issuelinks")
    public void setIssueLinks(List<IssueLink> issuelinks) {
        this.issuelinks = issuelinks;
    }

    /**
     * @return The assignee
     */
    @JsonProperty("assignee")
    public User getAssignee() {
        return assignee;
    }

    /**
     * @param assignee The assignee
     */
    @JsonProperty("assignee")
    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    /**
     * @return The status
     */
    @JsonProperty("status")
    public Status getStatus() {
        return status;
    }

    /**
     * @param status The status
     */
    @JsonProperty("status")
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return The components
     */
    @JsonProperty("components")
    public List<Component> getComponents() {
        return components;
    }

    /**
     * @param components The components
     */
    @JsonProperty("components")
    public void setComponents(List<Component> components) {
        this.components = components;
    }


    /**
     * @return The creator
     */
    @JsonProperty("creator")
    public User getCreator() {
        return creator;
    }

    /**
     * @param creator The creator
     */
    @JsonProperty("creator")
    public void setCreator(User creator) {
        this.creator = creator;
    }


    /**
     * @return The subtasks
     */
    @JsonProperty("subtasks")
    public List<Object> getSubtasks() {
        return subtasks;
    }

    /**
     * @param subtasks The subtasks
     */
    @JsonProperty("subtasks")
    public void setSubtasks(List<Object> subtasks) {
        this.subtasks = subtasks;
    }

    /**
     * @return The reporter
     */
    @JsonProperty("reporter")
    public User getReporter() {
        return reporter;
    }

    /**
     * @param reporter The reporter
     */
    @JsonProperty("reporter")
    public void setReporter(User reporter) {
        this.reporter = reporter;
    }


    /**
     * @return The progress
     */
    @JsonProperty("progress")
    public Progress getProgress() {
        return progress;
    }

    /**
     * @param progress The progress
     */
    @JsonProperty("progress")
    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    /**
     * @return The votes
     */
    @JsonProperty("votes")
    public Votes getVotes() {
        return votes;
    }

    /**
     * @param votes The votes
     */
    @JsonProperty("votes")
    public void setVotes(Votes votes) {
        this.votes = votes;
    }

    /**
     * @return The worklog
     */
    @JsonProperty("worklog")
    public Worklog getWorklog() {
        return worklog;
    }

    /**
     * @param worklog The worklog
     */
    @JsonProperty("worklog")
    public void setWorklog(Worklog worklog) {
        this.worklog = worklog;
    }

    /**
     * @return The issueType
     */
    @JsonProperty("issuetype")
    public IssueType getIssueType() {
        return issueType;
    }

    /**
     * @param issueType The issueType
     */
    @JsonProperty("issuetype")
    public void setIssueType(IssueType issueType) {
        this.issueType = issueType;
    }

    /**
     * @return The project
     */
    @JsonProperty("project")
    public Project getProject() {
        return project;
    }

    /**
     * @param project The project
     */
    @JsonProperty("project")
    public void setProject(Project project) {
        this.project = project;
    }


    /**
     * @return The watches
     */
    @JsonProperty("watches")
    public Watches getWatches() {
        return watches;
    }

    /**
     * @param watches The watches
     */
    @JsonProperty("watches")
    public void setWatches(Watches watches) {
        this.watches = watches;
    }


    /**
     * @return The created
     */
    @JsonProperty("created")
    public DateTime getCreated() {
        return created;
    }

    /**
     * @param created The created
     */
    @JsonProperty("created")
    public void setCreated(DateTime created) {
        this.created = created;
    }


    /**
     * @return The updated
     */
    @JsonProperty("updated")
    public DateTime getUpdated() {
        return updated;
    }

    /**
     * @param updated The updated
     */
    @JsonProperty("updated")
    public void setUpdated(DateTime updated) {
        this.updated = updated;
    }


    /**
     * @return The description
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @return The timetracking
     */
    @JsonProperty("timetracking")
    public TimeTracking getTimetracking() {
        return timetracking;
    }

    /**
     * @param timetracking The timetracking
     */
    @JsonProperty("timetracking")
    public void setTimetracking(TimeTracking timetracking) {
        this.timetracking = timetracking;
    }

    /**
     * @return The attachment
     */
    @JsonProperty("attachment")
    public List<Object> getAttachment() {
        return attachment;
    }

    /**
     * @param attachment The attachment
     */
    @JsonProperty("attachment")
    public void setAttachment(List<Object> attachment) {
        this.attachment = attachment;
    }

    /**
     * @return The summary
     */
    @JsonProperty("summary")
    public String getSummary() {
        return summary;
    }

    /**
     * @param summary The summary
     */
    @JsonProperty("summary")
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @return The duedate
     */
    @JsonProperty("duedate")
    public DateTime getDuedate() {
        return duedate;
    }

    /**
     * @param duedate The duedate
     */
    @JsonProperty("duedate")
    public void setDuedate(DateTime duedate) {
        this.duedate = duedate;
    }

    /**
     * @return The comment
     */
    @JsonProperty("comment")
    public Comments getComments() {
        return comments;
    }

    /**
     * @param comment The comment
     */
    @JsonProperty("comment")
    public void setComments(Comments comments) {
        this.comments = comments;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
